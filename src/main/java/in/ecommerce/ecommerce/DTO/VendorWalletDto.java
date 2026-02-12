package in.ecommerce.ecommerce.DTO;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class VendorWalletDto {
    private BigDecimal balance;
}
