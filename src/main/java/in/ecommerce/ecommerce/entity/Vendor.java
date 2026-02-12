package in.ecommerce.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vendors", indexes = {
                @Index(name = "idx_vendor_user", columnList = "user_id"),
                @Index(name = "idx_vendor_email", columnList = "business_email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @OneToOne
        @JoinColumn(name = "user_id", nullable = false, unique = true)
        private User user;

        @Column(nullable = false)
        private String businessName;

        @Column(nullable = false, unique = true)
        private String businessEmail;

        private String phoneNumber;

        private String address;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
}
