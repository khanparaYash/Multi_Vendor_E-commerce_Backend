package in.ecommerce.ecommerce.repo;

import in.ecommerce.ecommerce.entity.VendorWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VendorWalletRepository extends JpaRepository<VendorWallet, Long> {
    Optional<VendorWallet> findByVendorId(Long vendorId);
}
