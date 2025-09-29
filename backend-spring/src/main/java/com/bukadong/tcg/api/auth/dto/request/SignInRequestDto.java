package com.bukadong.tcg.api.auth.dto.request;

import com.bukadong.tcg.global.constant.ErrorMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignInRequestDto(
        @NotBlank(message = ErrorMessages.EMAIL_NOT_FOUND)
        @Email(message = ErrorMessages.INVALID_EMAIL)
        String email,

        @NotBlank(message = ErrorMessages.PASSWORD_NOT_FOUND)
        String password
) {
}
