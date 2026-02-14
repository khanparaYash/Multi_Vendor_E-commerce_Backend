package in.ecommerce.ecommerce.service;

import in.ecommerce.ecommerce.DTO.OrderDto;
import in.ecommerce.ecommerce.DTO.OrderItemDto;
import in.ecommerce.ecommerce.Redis.RedisStockService;
import in.ecommerce.ecommerce.entity.*;
import in.ecommerce.ecommerce.repo.CartRepo;
import in.ecommerce.ecommerce.repo.OrderRepo;
import in.ecommerce.ecommerce.repo.ProductRepo;
import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo orderRepo;
    private final CartRepo cartRepo;
    private final ProductRepo productRepo;
    private final RedisStockService redisStockService;

    // ===========================
    // CREATE ORDER
    // ===========================
    @Transactional
    public OrderDto createOrder(Long userId) {

        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // Prevent duplicate PAYMENT_PENDING orders
        // boolean existingPendingOrder =
        //         orderRepo.existsByUserIdAndStatusIn(
        //                 userId,
        //                 List.of(OrderStatus.CREATED, OrderStatus.PAYMENT_PENDING)
        //         );

        // if (existingPendingOrder) {
        //     throw new IllegalStateException("You already have a pending order.");
        // }

        Order order = Order.builder()
                .user(cart.getUser())
                .status(OrderStatus.CREATED)
                .totalAmount(cart.getTotalPrice())
                .reservedUntil(LocalDateTime.now().plusMinutes(15))
                .items(new ArrayList<>())
                .build();

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {

            Long productId = cartItem.getProduct().getId();

            // 1️⃣ Redis Reservation (Fast Layer)
            boolean reserved = redisStockService.reserveStock(
                    productId,
                    cartItem.getQuantity()
            );

            if (!reserved) {
                rollbackRedisReservations(orderItems);
                throw new IllegalStateException(
                        "Insufficient stock for product: "
                                + cartItem.getProduct().getName()
                );
            }

            // 2️⃣ DB Lock (Strong Consistency)
            Product product = productRepo.findByIdForUpdate(productId)
                    .orElseThrow(() -> new IllegalStateException("Product not found"));

            if (product.getStock() < cartItem.getQuantity()) {
                redisStockService.incrementStock(productId, cartItem.getQuantity());
                throw new IllegalStateException(
                        "Stock mismatch for product: " + product.getName()
                );
            }

            product.setStock(product.getStock() - cartItem.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .vendor(product.getVendor())
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(cartItem.getPrice())
                    .totalPrice(cartItem.getPrice() * cartItem.getQuantity())
                    .build();

            orderItems.add(orderItem);
        }

        order.setItems(orderItems);

        Order savedOrder = orderRepo.save(order);

        clearCart(cart);

        return mapToDto(savedOrder);
    }

    private void rollbackRedisReservations(List<OrderItem> items) {
        for (OrderItem item : items) {
            redisStockService.incrementStock(
                    item.getProduct().getId(),
                    item.getQuantity()
            );
        }
    }

    private void clearCart(Cart cart) {
        cart.getItems().clear();
        cart.setTotalPrice(0.0);
        cartRepo.save(cart);
    }

    // ===========================
    // UPDATE STATUS (STATE MACHINE)
    // ===========================
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found"));

        validateStateTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);

        if (newStatus == OrderStatus.CANCELLED) {
            releaseStock(order);
        }

        return mapToDto(orderRepo.save(order));
    }

    private void validateStateTransition(OrderStatus current, OrderStatus next) {

        Map<OrderStatus, List<OrderStatus>> validTransitions = Map.of(
                OrderStatus.CREATED, List.of(OrderStatus.PAYMENT_PENDING, OrderStatus.CANCELLED),
                OrderStatus.PAYMENT_PENDING, List.of(OrderStatus.PAID, OrderStatus.CANCELLED),
                OrderStatus.PAID, List.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
                OrderStatus.SHIPPED, List.of(OrderStatus.DELIVERED),
                OrderStatus.DELIVERED, List.of(),
                OrderStatus.CANCELLED, List.of()
        );

        if (!validTransitions.getOrDefault(current, List.of()).contains(next)) {
            throw new IllegalStateException(
                    "Invalid state transition: " + current + " → " + next
            );
        }
    }

    private void releaseStock(Order order) {
        for (OrderItem item : order.getItems()) {

            redisStockService.incrementStock(
                    item.getProduct().getId(),
                    item.getQuantity()
            );

            Product product = productRepo.findByIdForUpdate(
                    item.getProduct().getId()
            ).orElseThrow();

            product.setStock(product.getStock() + item.getQuantity());
        }
    }

    // ===========================
    // SHIPPING
    // ===========================
    @Transactional
    public OrderDto shipOrder(Long orderId, Long vendorId) {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found"));

        validateVendorOwnership(order, vendorId);

        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Order must be PAID before shipping.");
        }

        order.setStatus(OrderStatus.SHIPPED);
        return mapToDto(orderRepo.save(order));
    }

    @Transactional
    public OrderDto deliverOrder(Long orderId, Long vendorId) {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found"));

        validateVendorOwnership(order, vendorId);

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Order must be SHIPPED before delivery.");
        }

        order.setStatus(OrderStatus.DELIVERED);
        return mapToDto(orderRepo.save(order));
    }

    private void validateVendorOwnership(Order order, Long vendorId) {
        boolean authorized = order.getItems().stream()
                .anyMatch(i -> i.getVendor().getId().equals(vendorId));

        if (!authorized) {
            throw new IllegalStateException("Vendor not authorized for this order.");
        }
    }

    // ===========================
    // EXPIRED ORDER CLEANUP
    // ===========================
    @Transactional
    @Scheduled(fixedRate = 60000) 
    public void cancelExpiredOrders() {

        List<Order> expiredOrders =
                orderRepo.findByStatusInAndReservedUntilBefore(
                        List.of(OrderStatus.CREATED, OrderStatus.PAYMENT_PENDING),
                        LocalDateTime.now()
                );

        for (Order order : expiredOrders) {
            updateOrderStatus(order.getId(), OrderStatus.CANCELLED);
        }
    }

    // ===========================
    // MAPPING
    // ===========================
    private OrderDto mapToDto(Order order) {

        List<OrderItemDto> items = order.getItems().stream()
                .map(item -> OrderItemDto.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPriceAtPurchase())
                        .build())
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}
