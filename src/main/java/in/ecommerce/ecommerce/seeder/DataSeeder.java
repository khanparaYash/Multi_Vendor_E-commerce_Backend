package in.ecommerce.ecommerce.seeder;

import in.ecommerce.ecommerce.entity.*;
import in.ecommerce.ecommerce.repo.ProductRepo;
import in.ecommerce.ecommerce.repo.UserRepo;
import in.ecommerce.ecommerce.repo.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepo userRepo;
    private final VendorRepository vendorRepo;
    private final ProductRepo productRepo;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting Data Seeder...");

        User adminUser = seedAdmin();
        User vendorUser = seedVendorUser();
        Vendor vendor = seedVendorProfile(vendorUser);
        User customerUser = seedCustomer();
        seedProducts(vendor);

        log.info("Data Seeding Completed.");
    }

    private User seedAdmin() {
        User existingAdmin = userRepo.findByEmail("admin@example.com");
        if (existingAdmin != null) {
            log.info("Admin user already exists.");
            return existingAdmin;
        }

        User admin = User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .isVerified(true)
                .build();

        log.info("Created Admin user: admin@example.com");
        return userRepo.save(admin);
    }

    private User seedVendorUser() {
        User existingVendorUser = userRepo.findByEmail("vendor@example.com");
        if (existingVendorUser != null) {
            log.info("Vendor user already exists.");
            return existingVendorUser;
        }

        User vendor = User.builder()
                .email("vendor@example.com")
                .password(passwordEncoder.encode("vendor123"))
                .role(Role.VENDOR)
                .isVerified(true)
                .build();

        log.info("Created Vendor user: vendor@example.com");
        return userRepo.save(vendor);
    }

    private Vendor seedVendorProfile(User vendorUser) {
        Optional<Vendor> existingVendor = vendorRepo.findByUserId(vendorUser.getId());
        if (existingVendor.isPresent()) {
            log.info("Vendor profile already exists.");
            return existingVendor.get();
        }

        Vendor vendor = Vendor.builder()
                .user(vendorUser)
                .businessName("Tech Gadgets Inc.")
                .businessEmail("contact@techgadgets.com")
                .phoneNumber("1234567890")
                .address("123 Tech Street, Silicon Valley")
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();

        log.info("Created Vendor profile for: Tech Gadgets Inc.");
        return vendorRepo.save(vendor);
    }

    private User seedCustomer() {
        User existingCustomer = userRepo.findByEmail("customer@example.com");
        if (existingCustomer != null) {
            log.info("Customer user already exists.");
            return existingCustomer;
        }

        User customer = User.builder()
                .email("customer@example.com")
                .password(passwordEncoder.encode("customer123"))
                .role(Role.CUSTOMER)
                .isVerified(true)
                .build();

        log.info("Created Customer user: customer@example.com");
        return userRepo.save(customer);
    }

    private void seedProducts(Vendor vendor) {
        if (productRepo.count() > 0) {
            log.info("Products already exist. Skipping product seeding.");
            return;
        }

        List<Product> products = Arrays.asList(
                Product.builder()
                        .vendor(vendor)
                        .name("Smartphone X")
                        .description("Latest model with high-resolution camera and long battery life.")
                        .price(999.99)
                        .stock(50)
                        .status(ProductStatus.ACTIVE)
                        .averageRating(4.5)
                        .build(),
                Product.builder()
                        .vendor(vendor)
                        .name("Wireless Headphones")
                        .description("Noise-cancelling over-ear headphones.")
                        .price(199.50)
                        .stock(100)
                        .status(ProductStatus.ACTIVE)
                        .averageRating(4.7)
                        .build(),
                Product.builder()
                        .vendor(vendor)
                        .name("Gaming Laptop")
                        .description("High-performance laptop for gaming and creative work.")
                        .price(1499.00)
                        .stock(20)
                        .status(ProductStatus.ACTIVE)
                        .averageRating(4.8)
                        .build());

        productRepo.saveAll(products);
        log.info("Seeded " + products.size() + " products.");
    }
}
