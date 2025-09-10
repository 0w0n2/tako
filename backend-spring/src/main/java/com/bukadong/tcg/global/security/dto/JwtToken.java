package com.bukadong.tcg.global.security.dto;

import lombok.Builder;

@Builder
public record JwtToken(String grantType, String accessToken, String refreshToken) {
}
