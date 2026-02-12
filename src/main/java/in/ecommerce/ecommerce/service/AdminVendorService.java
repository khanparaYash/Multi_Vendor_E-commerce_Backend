package in.ecommerce.ecommerce.service;

import in.ecommerce.ecommerce.DTO.VendorDto;
import in.ecommerce.ecommerce.entity.ApprovalStatus;
import in.ecommerce.ecommerce.entity.Vendor;
import in.ecommerce.ecommerce.repo.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
// @PreAuthorize("hasRole('ADMIN')") // Better to handle security at Controller
// or Method level, but service level is also good.
public class AdminVendorService {

    @Autowired
    private VendorRepository vendorRepository;

    public List<VendorDto> getPendingVendors() {
        return vendorRepository.findByApprovalStatus(ApprovalStatus.PENDING).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public VendorDto approveVendor(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        vendor.setApprovalStatus(ApprovalStatus.APPROVED);
        return mapToDto(vendorRepository.save(vendor));
    }

    public VendorDto rejectVendor(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        vendor.setApprovalStatus(ApprovalStatus.REJECTED);
        return mapToDto(vendorRepository.save(vendor));
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
