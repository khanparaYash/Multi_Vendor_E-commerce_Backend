package in.ecommerce.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts", uniqueConstraints = {
                @UniqueConstraint(name = "uk_cart_user", columnNames = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
        @Builder.Default
        private List<CartItem> items = new ArrayList<>();

        @Builder.Default
        private Double totalPrice = 0.0;

        private String couponCode;
        @Builder.Default
        private Double discountAmount = 0.0;
}
