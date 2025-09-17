package com.bukadong.tcg.api.auth.dto.request;

import static com.bukadong.tcg.global.constant.ErrorMessages.*;

import com.bukadong.tcg.global.constant.Patterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 잃어버린 비밀번호 재설정 요청 DTO
 */
public record PasswordResetRequestDto(
        @NotBlank(message = EMAIL_NOT_FOUND)
        @Email(message = INVALID_EMAIL)
        String email,

        @NotBlank(message = PASSWORD_NOT_FOUND)
        @Pattern(regexp = Patterns.PASSWORD_REGEX, message = INVALID_PASSWORD)
        String password,

        @NotBlank(message = PASSWORD_RESET_CODE_NOT_FOUND)
        String passwordResetCode
) {
}
