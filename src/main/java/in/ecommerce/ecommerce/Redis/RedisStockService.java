package in.ecommerce.ecommerce.Redis;

import in.ecommerce.ecommerce.entity.Product;
import in.ecommerce.ecommerce.repo.ProductRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisStockService {

    private final RedisTemplate<String, Integer> redisTemplate;
    private final ProductRepo productRepo;

    public void loadStock(Long productId, Integer stock) {
        redisTemplate.opsForValue()
                .set("product:stock:" + productId, stock);
    }

    public boolean reserveStock(Long productId, int quantity) {

        String key = "product:stock:" + productId;
        Integer currentStock = redisTemplate.opsForValue().get(key);
        if (currentStock == null) {
            return false; // Redis not initialized
        }
        Long remaining = redisTemplate.opsForValue()
                .decrement(key, quantity);

        if (remaining == null || remaining < 0) {
            redisTemplate.opsForValue().increment(key, quantity);
            return false;
        }

        return true;
    }
    public boolean incrementStock(Long productId, int quantity) {
        String key = "product:stock:" + productId;
        redisTemplate.opsForValue().increment(key, quantity);
        return true;
    }

//    add database to redis all stock
    @PostConstruct //when application run and bean create that time this function will call only once
    public void syncAllProductsToRedis() {
        List<Product> products = productRepo.findAll();
        for (Product product : products) {
            loadStock(product.getId(), product.getStock());
        }
    }


}

