package com.tech.microservice.gateway.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, long ttlMillis) {
        redisTemplate.opsForValue().set(token, "blacklisted", Duration.ofMillis(ttlMillis));
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}
