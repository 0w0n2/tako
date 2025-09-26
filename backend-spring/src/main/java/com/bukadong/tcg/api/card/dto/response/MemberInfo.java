package com.bukadong.tcg.api.card.dto.response;

import com.bukadong.tcg.api.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record MemberInfo(
        @Schema(description = "지갑 주소")
        String walletAddress,
        @Schema(description = "닉네임")
        String nickname,
        @Schema(description = "이메일")
        String email
) {
    public static MemberInfo toDto(Member member, String walletAddress) {
        return MemberInfo.builder()
                .walletAddress(walletAddress)
                .nickname(member == null ? null : member.getNickname())
                .email(member == null ? null : member.getEmail())
                .build();
    }
}