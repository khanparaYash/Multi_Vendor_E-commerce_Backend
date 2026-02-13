package in.ecommerce.ecommerce.Redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Integer> redisTemplate(RedisConnectionFactory factory) {

        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Key as normal string
        template.setKeySerializer(new StringRedisSerializer());

        // Value as integer (NOT Java serialized object)
        template.setValueSerializer(new GenericToStringSerializer<>(Integer.class));

        template.afterPropertiesSet();
        return template;
    }
}
