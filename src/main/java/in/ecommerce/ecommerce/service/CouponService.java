package in.ecommerce.ecommerce.service;

import in.ecommerce.ecommerce.DTO.CouponDto;
import in.ecommerce.ecommerce.entity.Coupon;
import in.ecommerce.ecommerce.repo.CouponRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepo couponRepo;

    public CouponDto createCoupon(CouponDto couponDto) {
        if (couponRepo.existsByCode(couponDto.getCode())) {
            throw new RuntimeException("Coupon code already exists");
        }

        Coupon coupon = Coupon.builder()
                .code(couponDto.getCode())
                .discountPercentage(couponDto.getDiscountPercentage())
                .expiryDate(couponDto.getExpiryDate())
                .isActive(true)
                .build();

        return mapToDto(couponRepo.save(coupon));
    }

    public void deleteCoupon(Long id) {
        couponRepo.deleteById(id);
    }

    public List<CouponDto> getAllCoupons() {
        return couponRepo.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CouponDto mapToDto(Coupon coupon) {
        return CouponDto.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountPercentage(coupon.getDiscountPercentage())
                .expiryDate(coupon.getExpiryDate())
                .isActive(coupon.isActive())
                .build();
    }
}
