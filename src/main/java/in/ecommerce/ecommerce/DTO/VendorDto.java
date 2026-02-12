package in.ecommerce.ecommerce.DTO;

import in.ecommerce.ecommerce.entity.ApprovalStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorDto {
    private Long id;
    private Long userId;
    private String businessName;
    private String businessEmail;
    private String phoneNumber;
    private String address;
    private ApprovalStatus approvalStatus;
}
