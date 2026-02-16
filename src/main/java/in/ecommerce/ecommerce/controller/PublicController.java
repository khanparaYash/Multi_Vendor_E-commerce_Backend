package in.ecommerce.ecommerce.controller;

import in.ecommerce.ecommerce.DTO.ProductDto;
import in.ecommerce.ecommerce.entity.Product;
import in.ecommerce.ecommerce.entity.ProductStatus;
import in.ecommerce.ecommerce.repo.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final ProductRepo productRepo;

    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> getPublicProducts() {

        List<Product> products = productRepo.findByStatus(ProductStatus.ACTIVE);

        List<ProductDto> productDtos = products.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(productDtos);
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
