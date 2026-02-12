package in.ecommerce.ecommerce.service;

import in.ecommerce.ecommerce.DTO.ProductDto;
import in.ecommerce.ecommerce.entity.Product;
import in.ecommerce.ecommerce.repo.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepo productRepo;

    @Cacheable(value = "all_products", key = "'all'")
    public List<ProductDto> getAllProducts() {
        return productRepo.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "product", key = "#productId")
    public ProductDto getProductById(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToDto(product);
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
