package in.ecommerce.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "vendor_wallets", uniqueConstraints = {
                @UniqueConstraint(name = "uk_wallet_vendor", columnNames = "vendor_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorWallet extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "vendor_id", nullable = false)
        private Vendor vendor;

        @Column(nullable = false)
        private BigDecimal balance;

}
