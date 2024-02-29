package com.palgona.palgona.common.redis;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final RedisTemplate<String, String> redisTemplate;

    public void setBlacklist(String key, Long expirationTime) {
        redisTemplate.opsForValue().set(key, key, expirationTime, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklist(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
