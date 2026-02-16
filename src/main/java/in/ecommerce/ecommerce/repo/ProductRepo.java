package in.ecommerce.ecommerce.repo;

import in.ecommerce.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product, Long> {
    List<Product> findByVendorId(Long vendorId);

    org.springframework.data.domain.Page<Product> findByStatus(in.ecommerce.ecommerce.entity.ProductStatus status,
            org.springframework.data.domain.Pageable pageable);

    List<Product> findByStatus(in.ecommerce.ecommerce.entity.ProductStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

}
