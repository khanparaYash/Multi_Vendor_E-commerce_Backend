package in.ecommerce.ecommerce.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private Long userId;
    private Double totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItemDto> items;
}
