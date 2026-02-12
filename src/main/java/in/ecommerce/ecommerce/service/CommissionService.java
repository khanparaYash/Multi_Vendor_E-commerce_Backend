package in.ecommerce.ecommerce.service;

import in.ecommerce.ecommerce.entity.*;
import in.ecommerce.ecommerce.repo.OrderRepo;
import in.ecommerce.ecommerce.repo.VendorTransactionRepository;
import in.ecommerce.ecommerce.repo.VendorWalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CommissionService {

    @Autowired
    private VendorTransactionRepository vendorTransactionRepository;

    @Autowired
    private VendorWalletRepository vendorWalletRepository;

    @Autowired
    private OrderRepo orderRepo; // To save order status update if needed, though usually handled by OrderService

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.10"); // 10% commission

    @Transactional
    public void distributeCommission(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("Commission can only be distributed for PAID orders");
        }

        // In a real scenario, an order can have items from multiple vendors.
        // We need to iterate over items and group by vendor, or handle per item.
        // Assuming simple case: order items belong to vendors.

        for (OrderItem item : order.getItems()) {
            distributeForItem(item, order);
        }
    }

    private void distributeForItem(OrderItem item, Order order) {
        Vendor vendor = item.getVendor();
        BigDecimal totalItemPrice = BigDecimal.valueOf(item.getTotalPrice());

        BigDecimal commissionAmount = totalItemPrice.multiply(COMMISSION_RATE);
        BigDecimal netAmount = totalItemPrice.subtract(commissionAmount);

        // Update Wallet
        VendorWallet wallet = vendorWalletRepository.findByVendorId(vendor.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found for vendor: " + vendor.getId()));

        wallet.setBalance(wallet.getBalance().add(netAmount));
        vendorWalletRepository.save(wallet);

        // Record Transaction
        VendorTransaction transaction = VendorTransaction.builder()
                .vendor(vendor)
                .order(order)
                .amount(totalItemPrice)
                .commission(commissionAmount)
                .netAmount(netAmount)
                .build();

        vendorTransactionRepository.save(transaction);
    }
}
