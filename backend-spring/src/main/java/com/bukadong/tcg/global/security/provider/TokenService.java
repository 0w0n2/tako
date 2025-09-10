package com.bukadong.tcg.global.security.provider;

import com.bukadong.tcg.global.security.dto.JwtToken;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;

public interface TokenService {
    JwtToken generateToken(CustomUserDetails userDetails);

    JwtToken refresh(String refreshToken);

    void deleteRefreshToken(String memberUuid);
}
