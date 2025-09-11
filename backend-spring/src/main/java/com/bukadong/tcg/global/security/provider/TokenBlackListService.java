package com.bukadong.tcg.global.security.provider;

import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public interface TokenBlackListService {
    void addBlacklistAccessToken(String accessToken);

    void addBlacklistRefreshToken(String refreshToken);

    boolean isBlacklistAccessToken(String accessToken);

    boolean isBlacklistRefreshToken(String refreshToken);
}
