package com.bukadong.tcg.api.auth.dto.response;

import com.bukadong.tcg.global.mail.dto.VerificationCode;
import lombok.Builder;

/**
 * 이메일 코드 생성/전송 응답 DTO
 */
@Builder
public record EmailCodeResponseDto(
        String expiredAt    // 코드 만료 시간
) {
    public static EmailCodeResponseDto toDto(VerificationCode verificationCode) {
        return EmailCodeResponseDto.builder()
                .expiredAt(verificationCode.expiredAt())
                .build();
    }
}
