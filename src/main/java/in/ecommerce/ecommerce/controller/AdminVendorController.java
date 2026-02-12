package in.ecommerce.ecommerce.controller;

import in.ecommerce.ecommerce.DTO.VendorDto;
import in.ecommerce.ecommerce.service.AdminVendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/vendors")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVendorController {

    @Autowired
    private AdminVendorService adminVendorService;

    @GetMapping("/pending")
    public ResponseEntity<List<VendorDto>> getPendingVendors() {
        return ResponseEntity.ok(adminVendorService.getPendingVendors());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<VendorDto> approveVendor(@PathVariable Long id) {
        return ResponseEntity.ok(adminVendorService.approveVendor(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<VendorDto> rejectVendor(@PathVariable Long id) {
        return ResponseEntity.ok(adminVendorService.rejectVendor(id));
    }
}
