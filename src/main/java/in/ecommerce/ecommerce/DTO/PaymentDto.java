package in.ecommerce.ecommerce.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long id;
    private Long orderId;
    private String status;
    private Double amount;
    private String paymentReference;
}
