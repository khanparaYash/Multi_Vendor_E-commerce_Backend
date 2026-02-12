package in.ecommerce.ecommerce.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDto {
    private Long id;
    private String code;
    private Double discountPercentage;
    private LocalDate expiryDate;
    private boolean isActive;
}
