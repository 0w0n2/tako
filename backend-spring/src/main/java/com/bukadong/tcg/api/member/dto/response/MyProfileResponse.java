package com.bukadong.tcg.api.member.dto.response;

import com.bukadong.tcg.api.member.entity.Member;
import lombok.Builder;

@Builder
public record MyProfileResponse(Long memberId, String email, String nickname, String introduction,
        String profileImageUrl, String backgroundImageUrl, String walletAddress
// NotificationTypeCode.name(),
// value: 0|1
) {
    public static MyProfileResponse toDto(Member me, String profileImageUrl, String backgroundUrl) {
        return MyProfileResponse.builder().email(me.getEmail()).profileImageUrl(profileImageUrl)
                .backgroundImageUrl(backgroundUrl).memberId(me.getId()).nickname(me.getNickname())
                .introduction(me.getIntroduction()).walletAddress(me.getWalletAddress()).build();
    }
}
