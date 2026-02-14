package in.ecommerce.ecommerce.repo;

import in.ecommerce.ecommerce.entity.Order;
import in.ecommerce.ecommerce.entity.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    List<Order> findByItems_Product_Vendor_Id(Long vendorId);

    List<Order> findByStatusInAndReservedUntilBefore(List<OrderStatus> statuses,
            java.time.LocalDateTime now);
}
