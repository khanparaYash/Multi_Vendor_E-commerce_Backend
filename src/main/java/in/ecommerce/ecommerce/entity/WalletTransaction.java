package in.ecommerce.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wallet_transactions", indexes = {
                @Index(name = "idx_wallet_tx_vendor", columnList = "vendor_id"),
                @Index(name = "idx_wallet_tx_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "vendor_id", nullable = false)
        private Vendor vendor;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "order_id")
        private Order order;

        @Column(nullable = false)
        private Double amount;

        @Enumerated(EnumType.STRING)
        private TransactionType type;
}
