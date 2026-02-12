package in.ecommerce.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "coupons", uniqueConstraints = {
        @UniqueConstraint(columnNames = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Double discountPercentage;

    @Column(nullable = false)
    private LocalDate expiryDate;

    // Optional: Max usage count
    private Integer maxUsage;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
