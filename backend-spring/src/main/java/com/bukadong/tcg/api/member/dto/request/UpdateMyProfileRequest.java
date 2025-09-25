package com.bukadong.tcg.api.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "UpdateMyProfileRequest", description = "내 프로필 수정 요청. null 필드는 변경하지 않음")
public record UpdateMyProfileRequest(
        @Schema(description = "닉네임(2~10자)", example = "타코수집가") @Size(min = 2, max = 10) String nickname,
        @Schema(description = "자기소개(최대 255자)", example = "반갑습니다. 수집 좋아해요.") @Size(max = 255) String introduction) {
}
