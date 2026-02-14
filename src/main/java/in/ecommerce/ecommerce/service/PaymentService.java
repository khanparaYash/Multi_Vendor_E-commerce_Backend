package in.ecommerce.ecommerce.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import in.ecommerce.ecommerce.DTO.PaymentDto;
import in.ecommerce.ecommerce.entity.Order;
import in.ecommerce.ecommerce.entity.OrderStatus;
import in.ecommerce.ecommerce.entity.Payment;
import in.ecommerce.ecommerce.entity.PaymentStatus;
import in.ecommerce.ecommerce.repo.OrderRepo;
import in.ecommerce.ecommerce.repo.PaymentRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepo paymentRepo;
    private final OrderRepo orderRepo;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${app.name}")
    private String appName;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Transactional
    public PaymentDto createPaymentIntent(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new RuntimeException("Order is not in valid state for payment");
        }

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (order.getTotalAmount() * 100)) // Amount in cents
                    .setCurrency("usd") // Adjust currency as needed
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .putMetadata("order_id", String.valueOf(order.getId()))
                    .putMetadata("app_name", appName)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Create a pending payment record
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(order.getTotalAmount())
                    .paymentReference(paymentIntent.getId())
                    .paymentStatus(PaymentStatus.PENDING) // Initial status
                    .build();

            paymentRepo.save(payment);

            return PaymentDto.builder()
                    .id(payment.getId())
                    .orderId(order.getId())
                    .status(payment.getPaymentStatus().name())
                    .amount(payment.getAmount())
                    .paymentReference(payment.getPaymentReference())
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .currency(paymentIntent.getCurrency())
                    .build();

        } catch (StripeException e) {
            throw new RuntimeException("Error creating payment intent: " + e.getMessage(), e);
        }
    }
}
