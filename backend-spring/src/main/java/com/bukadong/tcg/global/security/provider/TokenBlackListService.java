package com.bukadong.tcg.global.security.provider;

import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public interface TokenBlackListService {
    void addBlacklistAccessToken(String accessToken, Date expiration);

    void addBlacklistRefreshToken(String refreshToken, Date expiration);

    boolean isBlacklistAccessToken(String accessToken);

    boolean isBlacklistRefreshToken(String refreshToken);
}
