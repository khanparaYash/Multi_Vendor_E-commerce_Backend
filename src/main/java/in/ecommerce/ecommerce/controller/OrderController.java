package in.ecommerce.ecommerce.controller;

import in.ecommerce.ecommerce.entity.Order;
import in.ecommerce.ecommerce.entity.OrderStatus;
import in.ecommerce.ecommerce.repo.OrderRepo;
import in.ecommerce.ecommerce.service.CommissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private CommissionService commissionService;

    // Simulating payment callback or admin action to mark order as PAID
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')") // Simplified security for demo
    @Transactional
    public ResponseEntity<String> markOrderPaid(@PathVariable Long id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.PAID) {
            return ResponseEntity.badRequest().body("Order is already paid");
        }

        order.setStatus(OrderStatus.PAID);
        orderRepo.save(order);

        // Trigger Commission Logic
        commissionService.distributeCommission(id);

        return ResponseEntity.ok("Order paid and commission distributed");
    }
}
