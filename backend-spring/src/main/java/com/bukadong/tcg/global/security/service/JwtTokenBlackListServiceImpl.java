package com.bukadong.tcg.global.security.service;

import com.bukadong.tcg.global.util.JwtTokenUtils;
import com.bukadong.tcg.global.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.bukadong.tcg.global.constant.SecurityConstants.*;

import java.time.Duration;
import java.util.Date;

/**
 * JWT 토큰을 블랙리스트에 추가하고 검증하는 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class JwtTokenBlackListServiceImpl implements JwtTokenBlackListService {
    private final RedisUtils redisUtils;

    private void addBlacklist(String prefix, String token, Date expiration) {
        String key = prefix + token;
        long now = System.currentTimeMillis();
        long remaining = expiration.getTime() - now;

        if (remaining > 0) {
            redisUtils.setValue(key, "", Duration.ofMillis(remaining)); // 남은 만료 시간까지 블랙리스트 처리
        }
    }

    @Override
    public void addBlacklistAccessToken(String accessToken, Date expiration) {
        addBlacklist(BLACKLIST_ACCESS_PREFIX, accessToken, expiration);
    }

    @Override
    public void addBlacklistRefreshToken(String refreshToken, Date expiration) {
        addBlacklist(BLACKLIST_REFRESH_PREFIX, refreshToken, expiration);
    }

    private boolean isBlacklist(String prefix, String token) {
        String key = prefix + token;
        return redisUtils.keyExists(key);
    }

    @Override
    public boolean isBlacklistAccessToken(String accessToken) {
        return isBlacklist(BLACKLIST_ACCESS_PREFIX, accessToken);
    }

    @Override
    public boolean isBlacklistRefreshToken(String refreshToken) {
        return isBlacklist(BLACKLIST_REFRESH_PREFIX, refreshToken);
    }
}
