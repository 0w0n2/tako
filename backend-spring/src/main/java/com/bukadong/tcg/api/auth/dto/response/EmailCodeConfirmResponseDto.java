package com.bukadong.tcg.api.auth.dto.response;

import lombok.Builder;

/**
 * 이메일 코드 검증 응답 DTO
 */
@Builder
public record EmailCodeConfirmResponseDto(
        boolean verified,
        boolean expired
) {
    public static EmailCodeConfirmResponseDto toDto(Boolean verified, Boolean expired) {
        return EmailCodeConfirmResponseDto.builder()
                .verified(verified)
                .expired(expired)
                .build();
    }
}
