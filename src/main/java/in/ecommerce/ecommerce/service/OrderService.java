package in.ecommerce.ecommerce.service;

import in.ecommerce.ecommerce.DTO.OrderDto;
import in.ecommerce.ecommerce.DTO.OrderItemDto;
import in.ecommerce.ecommerce.entity.*;
import in.ecommerce.ecommerce.repo.CartRepo;
import in.ecommerce.ecommerce.repo.OrderRepo;
import in.ecommerce.ecommerce.repo.ProductRepo;
import in.ecommerce.ecommerce.repo.UserRepo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final RedissonClient redissonClient;
    private final OrderRepo orderRepo;
    private final CartRepo cartRepo;
    private final ProductRepo productRepo;
    private final UserRepo userRepo;

    @Transactional
    public OrderDto createOrder(Long userId) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = Order.builder()
                .user(cart.getUser())
                .status(OrderStatus.CREATED)
                .totalAmount(cart.getTotalPrice())
                .items(new ArrayList<>())
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            String lockKey = "product_lock:" + cartItem.getProduct().getId();
            RLock lock = redissonClient.getLock(lockKey);

            try {
                // Try to acquire lock for 10 seconds, lease time 10 seconds
                if (lock.tryLock(10, 10, java.util.concurrent.TimeUnit.SECONDS)) {
                    try {
                        Product product = productRepo.findById(cartItem.getProduct().getId())
                                .orElseThrow(() -> new RuntimeException(
                                        "Product not found: " + cartItem.getProduct().getName()));

                        if (product.getStock() < cartItem.getQuantity()) {
                            throw new RuntimeException("Insufficient stock for product: " + product.getName());
                        }

                        // Reserve stock
                        product.setStock(product.getStock() - cartItem.getQuantity());
                        productRepo.save(product);

                        OrderItem orderItem = OrderItem.builder()
                                .order(order)
                                .product(product)
                                .quantity(cartItem.getQuantity())
                                .priceAtPurchase(cartItem.getPrice())
                                .vendor(product.getVendor())
                                .totalPrice(cartItem.getPrice()) // Ensure totalPrice is set if OrderItem expects it
                                .build();
                        orderItems.add(orderItem);
                    } finally {
                        lock.unlock();
                    }
                } else {
                    throw new RuntimeException(
                            "Could not acquire lock for product: " + cartItem.getProduct().getName());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread was interrupted while waiting for lock");
            }
        }

        order.setItems(orderItems);
        Order savedOrder = orderRepo.save(order);

        // Clear cart
        cart.getItems().clear();
        cart.setTotalPrice(0.0);
        cartRepo.save(cart);

        return mapToDto(savedOrder);
    }

    public OrderDto getOrder(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToDto(order);
    }

    public List<OrderDto> getCustomerOrders(Long userId) {
        return orderRepo.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<OrderDto> getVendorOrders(Long vendorId) {
        return orderRepo.findByItems_Product_Vendor_Id(vendorId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<OrderDto> getAllOrders() {
        return orderRepo.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Add valid transitions logic if needed
        order.setStatus(status);
        if (status == OrderStatus.CANCELLED) {
            // Release stock
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepo.save(product);
            }
        }

        return mapToDto(orderRepo.save(order));
    }

    public OrderDto cancelOrder(Long orderId) {
        return updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    @Transactional
    public OrderDto shipOrder(Long orderId, Long vendorId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify vendor is part of this order
        boolean isVendorInOrder = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getVendor().getId().equals(vendorId));

        if (!isVendorInOrder) {
            throw new RuntimeException("Vendor is not authorized to ship this order");
        }

        if (order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("Order cannot be shipped. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.SHIPPED);
        return mapToDto(orderRepo.save(order));
    }

    @Transactional
    public OrderDto deliverOrder(Long orderId, Long vendorId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify vendor is part of this order
        boolean isVendorInOrder = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getVendor().getId().equals(vendorId));

        if (!isVendorInOrder) {
            throw new RuntimeException("Vendor is not authorized to deliver this order");
        }

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("Order cannot be delivered. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.DELIVERED);
        return mapToDto(orderRepo.save(order));
    }

    private OrderDto mapToDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
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
                .items(itemDtos)
                .build();
    }
}
