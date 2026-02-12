package in.ecommerce.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items", indexes = {
                @Index(name = "idx_order_item_order", columnList = "order_id"),
                @Index(name = "idx_order_item_vendor", columnList = "vendor_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "order_id", nullable = false)
        private Order order;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "product_id", nullable = false)
        private Product product;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "vendor_id", nullable = false)
        private Vendor vendor;

        @Column(nullable = false)
        private Double priceAtPurchase;

        @Column(nullable = false)
        private Integer quantity;

        @Column(nullable = false)
        private Double totalPrice;
}
