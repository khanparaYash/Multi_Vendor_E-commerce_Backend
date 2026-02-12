package in.ecommerce.ecommerce.service;

import in.ecommerce.ecommerce.DTO.ReviewDto;
import in.ecommerce.ecommerce.entity.Order;
import in.ecommerce.ecommerce.entity.Product;
import in.ecommerce.ecommerce.entity.Review;
import in.ecommerce.ecommerce.entity.User;
import in.ecommerce.ecommerce.repo.OrderRepo;
import in.ecommerce.ecommerce.repo.ProductRepo;
import in.ecommerce.ecommerce.repo.ReviewRepo;
import in.ecommerce.ecommerce.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepo reviewRepo;
    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final OrderRepo orderRepo;

    @Transactional
    public ReviewDto addReview(Long userId, Long productId, Integer rating, String comment) {
        if (reviewRepo.existsByUserIdAndProductId(userId, productId)) {
            throw new RuntimeException("User has already reviewed this product");
        }

        // Verify purchase
        boolean hasPurchased = orderRepo.findByUserId(userId).stream()
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(productId));

        if (!hasPurchased) {
            throw new RuntimeException("User has not purchased this product");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(rating)
                .comment(comment)
                .build();

        reviewRepo.save(review);

        // Update product average rating
        Double avgRating = reviewRepo.findAverageRatingByProductId(productId);
        product.setAverageRating(avgRating != null ? avgRating : 0.0);
        productRepo.save(product);

        return mapToDto(review);
    }

    public List<ReviewDto> getReviewsByProduct(Long productId) {
        return reviewRepo.findByProductId(productId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ReviewDto mapToDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userName(review.getUser().getEmail()) // Assuming email or need to add name to User
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
