package in.ecommerce.ecommerce.aspect;

import in.ecommerce.ecommerce.annotation.RateLimit;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String ipAddress = request.getRemoteAddr();
        String methodName = joinPoint.getSignature().toShortString();

        // Key based on IP and Method to rate limit per user per endpoint
        String key = "rate_limit:" + methodName + ":" + ipAddress;

        RRateLimiter limiter = redissonClient.getRateLimiter(key);

        // Initialize if not exists (Lazy initialization)
        if (!limiter.isExists()) {
            // Using Duration based API
            limiter.trySetRate(RateType.OVERALL, rateLimit.limit(), java.time.Duration.ofSeconds(rateLimit.duration()));
            limiter.expire(java.time.Duration.ofSeconds(rateLimit.duration()));
        }

        if (limiter.tryAcquire()) {
            return joinPoint.proceed();
        } else {
            throw new RuntimeException("Too many requests - Rate limit exceeded");
        }
    }
}
