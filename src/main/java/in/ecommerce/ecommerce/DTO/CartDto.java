package in.ecommerce.ecommerce.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long id;
    private Double totalPrice;
    private List<CartItemDto> items;
}
