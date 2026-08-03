package com.noticecatch.api.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String BLACKLIST_PREFIX = "BL:";

    // Refresh Token 저장
    public void saveRefreshToken(Long userId, String refreshToken, long expirationMillis) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                Duration.ofMillis(expirationMillis)
        );
    }

    // Refresh Token 삭제 (로그아웃 시 사용)
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    // 저장된 Refresh Token 조회 (토큰 재발급 시 제출된 토큰과 대조용)
    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
    }

    // Access Token 블랙리스트 저장
    public void setBlackList(String accessToken, long expirationMillis) {
        if (expirationMillis > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + accessToken,
                    "logout",
                    Duration.ofMillis(expirationMillis)
            );
        }
    }

    // 블랙리스트 존재 여부 확인 (JwtAuthenticationFilter 등에서 사용)
    public boolean isBlackListed(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
    }
}
