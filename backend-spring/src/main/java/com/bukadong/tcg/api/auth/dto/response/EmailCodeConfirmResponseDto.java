package com.bukadong.tcg.api.auth.dto.response;

/**
 * 이메일 코드 검증 응답 DTO
 */
public record EmailCodeConfirmResponseDto(
        boolean verified,
        boolean expired,
        String passwordResetCode
) {
    public EmailCodeConfirmResponseDto(boolean verified, boolean expired) {
        this(verified, expired, null);
    }
}
