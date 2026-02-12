package in.ecommerce.ecommerce.repo;

import in.ecommerce.ecommerce.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepo extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);
}
