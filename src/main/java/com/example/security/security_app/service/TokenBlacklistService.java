package com.example.security.security_app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    // Blacklist token
    public void blacklistToken(String token, long expiryMs) {
        try {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(
                    key,
                    "blacklisted",
                    expiryMs,
                    TimeUnit.MILLISECONDS);
            log.info("Token blacklisted successfully");

        } catch (RedisConnectionFailureException e) {
            // Redis down — log but don't crash
            log.error("Redis unavailable — " +
                            "token blacklisting skipped: {}",
                    e.getMessage());
        }
    }

    // Check blacklist
    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(
                    redisTemplate.hasKey(key));

        } catch (RedisConnectionFailureException e) {
            // Redis down — allow request through
            // better to allow than block all users
            log.error("Redis unavailable — " +
                            "blacklist check skipped: {}",
                    e.getMessage());
            return false; // allow request
        }
    }
}