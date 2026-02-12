package in.ecommerce.ecommerce.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private Long productId;
    private String userName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
