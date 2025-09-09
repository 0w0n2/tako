package com.bukadong.tcg.global.security.service;

import com.bukadong.tcg.global.util.JwtTokenUtils;
import com.bukadong.tcg.global.util.RedisUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import static com.bukadong.tcg.global.constant.SecurityConstants.*;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtTokenBlackListServiceImpl implements JwtTokenBlackListService {
    private final RedisUtils redisUtils;
    private final JwtTokenUtils jwtTokenUtils;

    private void addBlacklist(String prefix, String token) {
        String key = prefix + token;
        Claims claims = jwtTokenUtils.parseClaims(token);
        Date expiration = claims.getExpiration();
        long now = new Date().getTime();
        long remaining = expiration.getTime() - now;
        if (remaining > 0) {
            redisUtils.setValue(key, "", Duration.ofMillis(remaining)); // 남은 만료 시간까지 블랙리스트 처리
        }
    }

    @Override
    public void addBlacklistAccessToken(String accessToken) {
        addBlacklist(BLACKLIST_ACCESS_PREFIX, accessToken);
    }

    @Override
    public void addBlacklistRefreshToken(String refreshToken) {
        addBlacklist(BLACKLIST_REFRESH_PREFIX, refreshToken);
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
