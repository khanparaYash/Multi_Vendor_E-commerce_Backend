package in.ecommerce.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products", indexes = {
                @Index(name = "idx_product_vendor", columnList = "vendor_id"),
                @Index(name = "idx_product_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "vendor_id", nullable = false)
        private Vendor vendor;

        @Column(nullable = false)
        private String name;

        @Column(length = 2000)
        private String description;

        @Column(nullable = false)
        private Double price;

        @Column(nullable = false)
        private Integer stock;

        @Enumerated(EnumType.STRING)
        private ProductStatus status;

        @Builder.Default
        private Double averageRating = 0.0;

        // 🔥 Optimistic Locking
        @Version
        private Long version;
}
