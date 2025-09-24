package com.bukadong.tcg.api.member.dto.response;

import com.bukadong.tcg.api.member.entity.Member;
import lombok.Builder;

import java.util.Map;

@Builder
public record MyProfileResponse(
        Long memberId,
        String email,
        String nickname,
        String introduction,
        String profileImageUrl,
        String backgroundImageUrl,
        Map<String, Integer> notificationSetting, // key:
        String walletAddress
        // NotificationTypeCode.name(),
        // value: 0|1
) {
    public static MyProfileResponse toDto(Member me, String  profileImageUrl, String backgroundUrl, Map<String, Integer> notificationSetting) {
        return MyProfileResponse.builder()
                .email(me.getEmail())
                .profileImageUrl(profileImageUrl)
                .backgroundImageUrl(backgroundUrl)
                .memberId(me.getId())
                .nickname(me.getNickname())
                .introduction(me.getIntroduction())
                .notificationSetting(notificationSetting)
                .walletAddress(me.getWalletAddress())
                .build();
    }
}
