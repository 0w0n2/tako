package com.bukadong.tcg.global.security.service;

import org.springframework.stereotype.Service;

@Service
public interface JwtTokenBlackListService {
    void addBlacklistAccessToken(String accessToken);

    void addBlacklistRefreshToken(String refreshToken);

    boolean isBlacklistAccessToken(String accessToken);

    boolean isBlacklistRefreshToken(String refreshToken);
}
