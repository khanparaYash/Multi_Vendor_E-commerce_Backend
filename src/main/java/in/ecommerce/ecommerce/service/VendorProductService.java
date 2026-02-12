package in.ecommerce.ecommerce.service;

import in.ecommerce.ecommerce.DTO.ProductDto;
import in.ecommerce.ecommerce.entity.ApprovalStatus;
import in.ecommerce.ecommerce.entity.Product;
import in.ecommerce.ecommerce.entity.ProductStatus;
import in.ecommerce.ecommerce.entity.Vendor;
import in.ecommerce.ecommerce.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import in.ecommerce.ecommerce.DTO.ProductDto;

@Service
public class VendorProductService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private VendorService vendorService;

    public ProductDto addProduct(ProductDto productDto) {
        Vendor vendor = vendorService.getVendorEntity();

        if (vendor.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new RuntimeException("Vendor is not approved to add products");
        }

        Product product = Product.builder()
                .vendor(vendor)
                .name(productDto.getName())
                .description(productDto.getDescription())
                .price(productDto.getPrice())
                .stock(productDto.getStock())
                .status(ProductStatus.ACTIVE)
                .build();

        Product savedProduct = productRepo.save(product);
        return mapToDto(savedProduct);
    }

    @Transactional
    public ProductDto updateProduct(Long productId, ProductDto updatedProductDto) {
        Vendor vendor = vendorService.getVendorEntity();
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getVendor().getId().equals(vendor.getId())) {
            throw new RuntimeException("You are not authorized to update this product");
        }

        if (updatedProductDto.getName() != null)
            product.setName(updatedProductDto.getName());
        if (updatedProductDto.getDescription() != null)
            product.setDescription(updatedProductDto.getDescription());
        if (updatedProductDto.getPrice() != null)
            product.setPrice(updatedProductDto.getPrice());
        if (updatedProductDto.getStock() != null)
            product.setStock(updatedProductDto.getStock());

        Product savedProduct = productRepo.save(product);
        return mapToDto(savedProduct);
    }

    public void deleteProduct(Long productId) {
        Vendor vendor = vendorService.getVendorEntity();
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getVendor().getId().equals(vendor.getId())) {
            throw new RuntimeException("You are not authorized to delete this product");
        }

        productRepo.delete(product);
    }

    // Fix: Use vendor ID from context as key or just don't cache here if context is
    // not available in key.
    // Since we get vendor from service, we can't easily put it in key unless we
    // pass it.
    // Better to remove @Cacheable here or use a custom key generator that looks at
    // SecurityContext.
    // For now, I will remove @Cacheable from here to avoid the bug, as it's less
    // critical than correctness.
    // @Cacheable("products")
    public List<ProductDto> getMyProducts() {
        Vendor vendor = vendorService.getVendorEntity();
        return productRepo.findByVendorId(vendor.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ProductDto mapToDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .vendorId(product.getVendor().getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .status(product.getStatus())
                .build();
    }
}
