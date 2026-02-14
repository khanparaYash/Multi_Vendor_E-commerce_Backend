package in.ecommerce.ecommerce.controller;

import in.ecommerce.ecommerce.DTO.*;
import in.ecommerce.ecommerce.entity.User;
import in.ecommerce.ecommerce.repo.UserRepo;
import in.ecommerce.ecommerce.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;
    private final UserRepo userRepo; // To get user ID from Principal

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user.getId();
    }

    // --- Cart APIs ---

    @PostMapping("/cart/add")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CartDto> addToCart(@RequestBody AddToCartRequest request) {
        return ResponseEntity
                .ok(cartService.addToCart(getCurrentUserId(), request.getProductId(), request.getQuantity()));
    }

    @PutMapping("/cart/update")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CartDto> updateCart(@RequestBody AddToCartRequest request) {
        return ResponseEntity
                .ok(cartService.updateCart(getCurrentUserId(), request.getProductId(), request.getQuantity()));
    }

    @DeleteMapping("/cart/remove/{productId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CartDto> removeFromCart(@PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeFromCart(getCurrentUserId(), productId));
    }

    @GetMapping("/cart")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CartDto> getCart() {
        return ResponseEntity.ok(cartService.getCart(getCurrentUserId()));
    }

    @PostMapping("/cart/apply-coupon")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CartDto> applyCoupon(@RequestParam String code) {
        return ResponseEntity.ok(cartService.applyCoupon(getCurrentUserId(), code));
    }

    // --- Order APIs ---

    @PostMapping("/order/create")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<OrderDto> createOrder() {
        return ResponseEntity.ok(orderService.createOrder(getCurrentUserId()));
    }

    @GetMapping("/order/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long id) {
        OrderDto order = orderService.getOrder(id);
        if (!order.getUserId().equals(getCurrentUserId())) {
            throw new RuntimeException("Access Denied");
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<List<OrderDto>> getOrders() {
        return ResponseEntity.ok(orderService.getCustomerOrders(getCurrentUserId()));
    }

    // --- Payment APIs ---

    @PostMapping("/payment/{orderId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<PaymentDto> processPayment(@PathVariable Long orderId) {
        // Security check: order must belong to user
        OrderDto order = orderService.getOrder(orderId);
        if (!order.getUserId().equals(getCurrentUserId())) {
            throw new RuntimeException("Access Denied");
        }
        return ResponseEntity.ok(paymentService.createPaymentIntent(orderId));
    }

    // --- Review APIs ---

    @PostMapping("/review")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ReviewDto> addReview(@RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.addReview(getCurrentUserId(), request.getProductId(),
                request.getRating(), request.getComment()));
    }
}
