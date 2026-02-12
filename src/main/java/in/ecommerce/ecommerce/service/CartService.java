package in.ecommerce.ecommerce.service;

import in.ecommerce.ecommerce.DTO.CartDto;
import in.ecommerce.ecommerce.DTO.CartItemDto;
import in.ecommerce.ecommerce.entity.Cart;
import in.ecommerce.ecommerce.entity.CartItem;
import in.ecommerce.ecommerce.entity.Product;
import in.ecommerce.ecommerce.entity.User;
import in.ecommerce.ecommerce.entity.Coupon;
import in.ecommerce.ecommerce.repo.CartItemRepo;
import in.ecommerce.ecommerce.repo.CartRepo;
import in.ecommerce.ecommerce.repo.CouponRepo;
import in.ecommerce.ecommerce.repo.ProductRepo;
import in.ecommerce.ecommerce.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

        private final CartRepo cartRepo;
        private final CartItemRepo cartItemRepo;
        private final ProductRepo productRepo;
        private final UserRepo userRepo;
        private final CouponRepo couponRepo;

        @Transactional
        public CartDto addToCart(Long userId, Long productId, Integer quantity) {
                User user = userRepo.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                Product product = productRepo.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                if (product.getStock() < quantity) {
                        throw new RuntimeException("Insufficient stock");
                }

                Cart cart = cartRepo.findByUserId(userId)
                                .orElseGet(() -> {
                                        Cart newCart = Cart.builder().user(user).items(new ArrayList<>())
                                                        .totalPrice(0.0).build();
                                        return cartRepo.save(newCart);
                                });

                Optional<CartItem> existingItem = cart.getItems().stream()
                                .filter(item -> item.getProduct().getId().equals(productId))
                                .findFirst();

                if (existingItem.isPresent()) {
                        CartItem item = existingItem.get();
                        item.setQuantity(item.getQuantity() + quantity);
                        item.setPrice(item.getProduct().getPrice() * item.getQuantity());
                } else {
                        CartItem newItem = CartItem.builder()
                                        .cart(cart)
                                        .product(product)
                                        .quantity(quantity)
                                        .price(product.getPrice() * quantity)
                                        .build();
                        cart.getItems().add(newItem);
                }

                updateCartTotal(cart);
                cartRepo.save(cart);

                return mapToDto(cart);
        }

        @Transactional
        public CartDto updateCart(Long userId, Long productId, Integer quantity) {
                Cart cart = cartRepo.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException("Cart not found"));

                CartItem item = cart.getItems().stream()
                                .filter(i -> i.getProduct().getId().equals(productId))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

                Product product = item.getProduct();
                if (product.getStock() < quantity) {
                        throw new RuntimeException("Insufficient stock");
                }

                item.setQuantity(quantity);
                item.setPrice(product.getPrice() * quantity);

                updateCartTotal(cart);
                cartRepo.save(cart);
                return mapToDto(cart);
        }

        @Transactional
        public CartDto removeFromCart(Long userId, Long productId) {
                Cart cart = cartRepo.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException("Cart not found"));

                CartItem itemToRemove = cart.getItems().stream()
                                .filter(item -> item.getProduct().getId().equals(productId))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

                cart.getItems().remove(itemToRemove);
                cartItemRepo.delete(itemToRemove); // Explicit delete just in case, though orphanRemoval should handle
                                                   // it

                updateCartTotal(cart);
                cartRepo.save(cart);
                return mapToDto(cart);
        }

        public CartDto getCart(Long userId) {
                Cart cart = cartRepo.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException("Cart not found"));
                return mapToDto(cart);
        }

        public CartDto applyCoupon(Long userId, String code) {
                Cart cart = cartRepo.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException("Cart not found"));

                Coupon coupon = couponRepo.findByCode(code)
                                .orElseThrow(() -> new RuntimeException("Coupon not found"));

                if (!coupon.isActive() || coupon.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                        throw new RuntimeException("Coupon is invalid or expired");
                }

                cart.setCouponCode(code);

                double discount = (cart.getTotalPrice() * coupon.getDiscountPercentage()) / 100;
                cart.setDiscountAmount(discount);

                // Recalculate total with discount (or keep total price as subtotal and have a
                // final total?)
                // Requirements say "Recalculate total price automatically".
                // Usually total price = sum of items. Discount is applied on top.
                // Let's keep totalPrice as sum of items, and maybe add a "finalPrice" or just
                // return discount info in DTO.
                // I will update mapToDto to reflect discount.

                cartRepo.save(cart);
                return mapToDto(cart);
        }

        private void updateCartTotal(Cart cart) {
                double total = cart.getItems().stream()
                                .mapToDouble(CartItem::getPrice)
                                .sum();
                cart.setTotalPrice(total);
                // Re-apply discount if coupon exists
                if (cart.getCouponCode() != null && !cart.getCouponCode().isEmpty()) {
                        couponRepo.findByCode(cart.getCouponCode()).ifPresent(coupon -> {
                                if (coupon.isActive() && !coupon.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                                        double discount = (total * coupon.getDiscountPercentage()) / 100;
                                        cart.setDiscountAmount(discount);
                                } else {
                                        cart.setCouponCode(null);
                                        cart.setDiscountAmount(0.0);
                                }
                        });
                }
        }

        private CartDto mapToDto(Cart cart) {
                List<CartItemDto> itemDtos = cart.getItems().stream()
                                .map(item -> CartItemDto.builder()
                                                .id(item.getId())
                                                .productId(item.getProduct().getId())
                                                .productName(item.getProduct().getName())
                                                .quantity(item.getQuantity())
                                                .price(item.getPrice())
                                                .build())
                                .collect(Collectors.toList());

                return CartDto.builder()
                                .id(cart.getId())
                                .totalPrice(cart.getTotalPrice()
                                                - (cart.getDiscountAmount() != null ? cart.getDiscountAmount() : 0.0)) // Return
                                                                                                                       // final
                                                                                                                       // price
                                .items(itemDtos)
                                .build();
        }
}
