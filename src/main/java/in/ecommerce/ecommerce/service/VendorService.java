package in.ecommerce.ecommerce.service;

import in.ecommerce.ecommerce.DTO.VendorDto;
import in.ecommerce.ecommerce.DTO.VendorRegistrationDto;
import in.ecommerce.ecommerce.entity.ApprovalStatus;
import in.ecommerce.ecommerce.entity.Role;
import in.ecommerce.ecommerce.entity.User;
import in.ecommerce.ecommerce.entity.Vendor;
import in.ecommerce.ecommerce.entity.VendorWallet;
import in.ecommerce.ecommerce.repo.VendorRepository;
import in.ecommerce.ecommerce.repo.VendorWalletRepository;
import in.ecommerce.ecommerce.repo.UserRepo; // Assuming UserRepo exists in this package
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private VendorWalletRepository vendorWalletRepository;

    @Autowired
    private UserRepo userRepo;

    @Transactional
    public VendorDto registerVendor(VendorRegistrationDto registrationDto) {
        String userEmail = getCurrentUserEmail();
        User user = userRepo.findByEmail(userEmail);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.getRole() != Role.VENDOR) {
            throw new RuntimeException("Only users with VENDOR role can register as a vendor");
        }

        if (vendorRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("Vendor profile already exists for this user");
        }

        if (vendorRepository.findByBusinessEmail(registrationDto.getBusinessEmail()).isPresent()) {
            throw new RuntimeException("Business email already in use");
        }

        Vendor vendor = Vendor.builder()
                .user(user)
                .businessName(registrationDto.getBusinessName())
                .businessEmail(registrationDto.getBusinessEmail())
                .phoneNumber(registrationDto.getPhoneNumber())
                .address(registrationDto.getAddress())
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        Vendor savedVendor = vendorRepository.save(vendor);

        // Initialize Wallet
        VendorWallet wallet = VendorWallet.builder()
                .vendor(savedVendor)
                .balance(BigDecimal.ZERO)
                .build();
        vendorWalletRepository.save(wallet);

        return mapToDto(savedVendor);
    }

    public VendorDto getCurrentVendorProfile() {
        String email = getCurrentUserEmail();
        User user = userRepo.findByEmail(email);
        Vendor vendor = vendorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Vendor profile not found"));
        return mapToDto(vendor);
    }

    @Transactional
    public VendorDto updateVendorProfile(VendorDto vendorDto) {
        String email = getCurrentUserEmail();
        User user = userRepo.findByEmail(email);
        Vendor vendor = vendorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Vendor profile not found"));

        if (vendorDto.getBusinessName() != null)
            vendor.setBusinessName(vendorDto.getBusinessName());
        if (vendorDto.getPhoneNumber() != null)
            vendor.setPhoneNumber(vendorDto.getPhoneNumber());
        if (vendorDto.getAddress() != null)
            vendor.setAddress(vendorDto.getAddress());

        // Business Email update logic should be careful or restricted
        // Keeping it simple for now (no update to email usually allowed easily)

        return mapToDto(vendorRepository.save(vendor));
    }

    public Vendor getVendorEntity() {
        String email = getCurrentUserEmail();
        User user = userRepo.findByEmail(email);
        return vendorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Vendor profile not found"));
    }

    public in.ecommerce.ecommerce.DTO.VendorWalletDto getVendorWallet() {
        Vendor vendor = getVendorEntity();
        VendorWallet wallet = vendorWalletRepository.findByVendorId(vendor.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return in.ecommerce.ecommerce.DTO.VendorWalletDto.builder()
                .balance(wallet.getBalance())
                .build();
    }

    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private VendorDto mapToDto(Vendor vendor) {
        return VendorDto.builder()
                .id(vendor.getId())
                .userId(vendor.getUser().getId())
                .businessName(vendor.getBusinessName())
                .businessEmail(vendor.getBusinessEmail())
                .phoneNumber(vendor.getPhoneNumber())
                .address(vendor.getAddress())
                .approvalStatus(vendor.getApprovalStatus())
                .build();
    }
}
