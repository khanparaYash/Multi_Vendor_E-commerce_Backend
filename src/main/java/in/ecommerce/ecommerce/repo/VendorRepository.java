package in.ecommerce.ecommerce.repo;

import in.ecommerce.ecommerce.entity.ApprovalStatus;
import in.ecommerce.ecommerce.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByBusinessEmail(String businessEmail);

    Optional<Vendor> findByUserId(Long userId);

    List<Vendor> findByApprovalStatus(ApprovalStatus approvalStatus);
}
