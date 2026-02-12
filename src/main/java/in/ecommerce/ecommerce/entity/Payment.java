package in.ecommerce.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_order", columnNames = "order_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String paymentReference;

    @Column(nullable = false)
    private Double amount;
}
