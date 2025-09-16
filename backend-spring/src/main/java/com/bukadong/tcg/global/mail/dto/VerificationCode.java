package com.bukadong.tcg.global.mail.dto;

import lombok.Builder;

@Builder
public record VerificationCode(
        String code,
        String expiredAt
) {
    public static VerificationCode toDto(String code, String expiredAt) {
        return VerificationCode.builder()
                .code(code)
                .expiredAt(expiredAt)
                .build();
    }
}
