package in.ecommerce.ecommerce.repo;

import in.ecommerce.ecommerce.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepo extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);

    Payment findByPaymentReference(String paymentReference);
}
