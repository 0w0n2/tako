package com.bukadong.tcg.api.auth.dto.request;

import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.entity.Role;
import com.bukadong.tcg.global.constant.ErrorMessages;
import com.bukadong.tcg.global.constant.Patterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
@Schema(description = "회원가입 요청 DTO")
public record SignUpRequestDto(
        @Email(message = ErrorMessages.INVALID_EMAIL)
        @NotBlank(message = ErrorMessages.EMAIL_NOT_FOUND)
        String email,

        @NotBlank(message = ErrorMessages.PASSWORD_NOT_FOUND)
        @Pattern(regexp = Patterns.PASSWORD_REGEX, message = ErrorMessages.INVALID_PASSWORD)
        String password,

        @NotBlank(message = ErrorMessages.NICKNAME_NOT_FOUND)
        @Pattern(regexp = Patterns.NICKNAME_REGEX, message = ErrorMessages.INVALID_NICKNAME)
        String nickname,

        @NotNull(message = ErrorMessages.IS_SOCIAL_NOT_FOUND)
        Boolean isSocial,

        String providerName
) {
    public Member toMember(String memberUuid, String encodedPassword) {
        return Member.builder()
                .uuid(memberUuid)
                .email(this.email)
                .password(encodedPassword)
                .nickname(this.nickname)
                .introduction("")   //TODO: introduction nullable=false라 빈값 넣었는데, 이후 프론트팀과 회의 필요
                .role(Role.USER)
                .build();
    }
}
