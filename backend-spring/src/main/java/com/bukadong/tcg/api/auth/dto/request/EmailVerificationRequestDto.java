package com.bukadong.tcg.api.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationRequestDto(
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email
        String email,

        @NotBlank(message = "인증 타입을 입력해주세요.")
        String verificationType
) {
}
