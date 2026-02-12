package in.ecommerce.ecommerce.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    private Long productId;
    private Integer rating;
    private String comment;
}
