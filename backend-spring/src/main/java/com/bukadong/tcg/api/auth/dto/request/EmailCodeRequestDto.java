package com.bukadong.tcg.api.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import static com.bukadong.tcg.global.constant.ErrorMessages.*;

public record EmailCodeRequestDto(
        @NotBlank(message = EMAIL_NOT_FOUND)
        @Email(message = INVALID_EMAIL)
        String email,

        @NotBlank(message = VERIFICATION_TYPE_NOT_FOUND)
        String verificationType
) {
}
