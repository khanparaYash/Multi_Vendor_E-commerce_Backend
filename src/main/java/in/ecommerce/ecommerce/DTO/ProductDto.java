package in.ecommerce.ecommerce.DTO;

import in.ecommerce.ecommerce.entity.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ProductDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long vendorId;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private ProductStatus status;
}
