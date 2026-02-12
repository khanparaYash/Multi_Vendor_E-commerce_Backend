package in.ecommerce.ecommerce.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    private Long productId;
    private Integer quantity;
}
