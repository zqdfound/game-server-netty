package com.mars.dor.netty.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author zhuangqingdian
 * @date 2022/8/16
 */
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 把默认的序列化器改成StringRedisSerializer
        RedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setDefaultSerializer(stringSerializer);
        return redisTemplate;
    }
}
