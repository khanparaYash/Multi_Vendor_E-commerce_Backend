package in.ecommerce.ecommerce.repo;

import in.ecommerce.ecommerce.entity.VendorTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorTransactionRepository extends JpaRepository<VendorTransaction, Long> {
    List<VendorTransaction> findByVendorId(Long vendorId);
}
