package in.ecommerce.ecommerce.repo;

import in.ecommerce.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepo extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
}
