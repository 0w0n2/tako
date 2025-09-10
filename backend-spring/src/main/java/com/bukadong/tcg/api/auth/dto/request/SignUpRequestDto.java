package com.bukadong.tcg.api.auth.dto.request;

import com.bukadong.tcg.global.constant.ErrorMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "회원가입 요청 DTO")
public record SignUpRequestDto(
        @Email(message = ErrorMessages.INVALID_EMAIL)
        @NotBlank(message = ErrorMessages.EMAIL_NOT_FOUND)
        String email,

        @NotBlank(message = ErrorMessages.PASSWORD_NOT_FOUND)
        String password,

        @NotBlank(message = ErrorMessages.NICKNAME_NOT_FOUND)
        String nickname,
        Boolean isSocial,
        String providerName
) {
}
