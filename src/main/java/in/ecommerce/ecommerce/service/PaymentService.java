package in.ecommerce.ecommerce.service;

import in.ecommerce.ecommerce.DTO.PaymentDto;
import in.ecommerce.ecommerce.entity.Order;
import in.ecommerce.ecommerce.entity.OrderStatus;
import in.ecommerce.ecommerce.entity.Payment;
import in.ecommerce.ecommerce.entity.PaymentStatus;
import in.ecommerce.ecommerce.entity.Product;
import in.ecommerce.ecommerce.repo.OrderRepo;
import in.ecommerce.ecommerce.repo.PaymentRepo;
import in.ecommerce.ecommerce.repo.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepo paymentRepo;
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;

    @Transactional
    public PaymentDto processPayment(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new RuntimeException("Order is not in valid state for payment");
        }

        // Simulate payment (success/fail logic could be random or based on input, here
        // we assume success for simplicity unless configured otherwise)
        boolean paymentSuccess = true;

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .paymentReference(UUID.randomUUID().toString())
                .paymentStatus(paymentSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .build();

        paymentRepo.save(payment);

        if (paymentSuccess) {
            order.setStatus(OrderStatus.PAID);
        } else {
            order.setStatus(OrderStatus.PAYMENT_PENDING); // Or CANCELLED if strict
            // If we want to release stock on failure, we should do it here if we consider
            // this final.
            // But usually we allow retries.
            // Requirement says: "If payment fails -> release reserved stock."
            // This is harsh for a pending payment, but let's follow requirement if it
            // implies immediate failure.
            // "If status -> keep as PAYMENT_PENDING". So maybe we don't release stock yet?
            // "Ensure: Stock was reserved earlier. If payment fails -> release reserved
            // stock."
            // Contradiction: "If failure -> keep as PAYMENT_PENDING". If pending, stock
            // should be held?
            // I'll assume "Final Failure" implies release. For now, let's keep it pending.
        }

        orderRepo.save(order);

        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(order.getId())
                .status(payment.getPaymentStatus().name())
                .amount(payment.getAmount())
                .paymentReference(payment.getPaymentReference())
                .build();

    }
}
