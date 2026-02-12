package in.ecommerce.ecommerce.controller;

import in.ecommerce.ecommerce.DTO.CouponDto;
import in.ecommerce.ecommerce.DTO.OrderDto;
import in.ecommerce.ecommerce.service.CouponService;
import in.ecommerce.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final OrderService orderService;
    private final CouponService couponService;

    // --- Order Management ---

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    // --- Coupon Management ---

    @PostMapping("/coupons")
    public ResponseEntity<CouponDto> createCoupon(@RequestBody CouponDto couponDto) {
        return ResponseEntity.ok(couponService.createCoupon(couponDto));
    }

    @GetMapping("/coupons")
    public ResponseEntity<List<CouponDto>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok().build();
    }
}
