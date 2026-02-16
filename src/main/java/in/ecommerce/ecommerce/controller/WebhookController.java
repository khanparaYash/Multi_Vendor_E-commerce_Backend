package in.ecommerce.ecommerce.controller;

import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import in.ecommerce.ecommerce.entity.Order;
import in.ecommerce.ecommerce.entity.OrderStatus;
import in.ecommerce.ecommerce.entity.Payment;
import in.ecommerce.ecommerce.entity.PaymentStatus;
import in.ecommerce.ecommerce.repo.OrderRepo;
import in.ecommerce.ecommerce.repo.PaymentRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stripe/webhook")

@Slf4j
public class WebhookController {
    @Autowired
    private  PaymentRepo paymentRepo;
    @Autowired
    private  OrderRepo orderRepo;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Value("${app.name}")
    private String appName;

    @PostMapping
    public ResponseEntity<String> handleStripeEvent(@RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        if (sigHeader == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }

        Event event;

        try {
            event = Webhook.constructEvent(
                    payload, sigHeader, endpointSecret);
        } catch (Exception e) {
            log.error("Webhook error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }

        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

        if (paymentIntent != null) {
            String intentAppName = paymentIntent.getMetadata().get("app_name");
            if (!appName.equals(intentAppName)) {
                log.info("Ignoring event for different app: " + intentAppName);
                return ResponseEntity.ok("");
            }
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            if (paymentIntent != null) {
                handlePaymentSuccess(paymentIntent);
            }
        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            if (paymentIntent != null) {
                handlePaymentFailure(paymentIntent);
            }
        }

        return ResponseEntity.ok("");
    }
    @Transactional
    public void handlePaymentSuccess(PaymentIntent paymentIntent) {
        String paymentIntentId = paymentIntent.getId();
        Payment payment = paymentRepo.findByPaymentReference(paymentIntentId);

        if (payment != null) {
            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                log.info("Payment already processed. Skipping.");
                return;
            }

            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            paymentRepo.save(payment);

            Order order = payment.getOrder();
            order.setStatus(OrderStatus.PAID);
            orderRepo.save(order);
            log.info("Payment succeeded for Order ID: " + order.getId());
        } else {
            log.warn("Payment not found for Intent ID: " + paymentIntentId);
        }
    }
    @Transactional
    public void handlePaymentFailure(PaymentIntent paymentIntent) {
        String paymentIntentId = paymentIntent.getId();
        Payment payment = paymentRepo.findByPaymentReference(paymentIntentId);

        if (payment != null) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepo.save(payment);

            // Optional: Cancel order or keep pending
            // Order order = payment.getOrder();
            // order.setStatus(OrderStatus.CANCELLED);
            // orderRepo.save(order);
            log.info("Payment failed for Payment ID: " + payment.getId());
        }
    }
}
