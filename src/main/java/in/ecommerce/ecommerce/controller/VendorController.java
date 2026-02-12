package in.ecommerce.ecommerce.controller;

import in.ecommerce.ecommerce.DTO.*;
import in.ecommerce.ecommerce.service.VendorProductService;
import in.ecommerce.ecommerce.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendor")
@PreAuthorize("hasRole('VENDOR')")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @Autowired
    private VendorProductService vendorProductService;

    @PostMapping("/register")
    public ResponseEntity<VendorDto> registerVendor(@RequestBody VendorRegistrationDto registrationDto) {
        return ResponseEntity.ok(vendorService.registerVendor(registrationDto));
    }

    @GetMapping("/profile")
    public ResponseEntity<VendorDto> getProfile() {
        return ResponseEntity.ok(vendorService.getCurrentVendorProfile());
    }

    @PutMapping("/update")
    public ResponseEntity<VendorDto> updateProfile(@RequestBody VendorDto vendorDto) {
        return ResponseEntity.ok(vendorService.updateVendorProfile(vendorDto));
    }

    @GetMapping("/my-products")
    public ResponseEntity<List<ProductDto>> getMyProducts() {
        return ResponseEntity.ok(vendorProductService.getMyProducts());
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDto> addProduct(@RequestBody ProductDto productDto) {
        return ResponseEntity.ok(vendorProductService.addProduct(productDto));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto) {
        return ResponseEntity.ok(vendorProductService.updateProduct(id, productDto));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        vendorProductService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/wallet")
    public ResponseEntity<VendorWalletDto> getWallet() {
        return ResponseEntity.ok(vendorService.getVendorWallet());
    }

    // --- Order Management ---
    @Autowired
    private in.ecommerce.ecommerce.service.OrderService orderService;

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDto>> getVendorOrders() {
        // Need to implement getting current vendor ID
        Long vendorId = vendorService.getVendorEntity().getId();
        return ResponseEntity.ok(orderService.getVendorOrders(vendorId));
    }

    @PutMapping("/orders/{id}/ship")
    public ResponseEntity<OrderDto> shipOrder(@PathVariable Long id) {
        Long vendorId = vendorService.getVendorEntity().getId();
        return ResponseEntity.ok(orderService.shipOrder(id, vendorId));
    }

    @PutMapping("/orders/{id}/deliver")
    public ResponseEntity<OrderDto> deliverOrder(@PathVariable Long id) {
        Long vendorId = vendorService.getVendorEntity().getId();
        return ResponseEntity.ok(orderService.deliverOrder(id, vendorId));
    }
}
