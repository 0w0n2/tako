package com.bukadong.tcg.api.member.dto.response;

import java.util.Map;

public record MyProfileResponse(Long memberId, String email, String nickname, String introduction,
        String profileImageUrl, String backgroundImageUrl, Map<String, Integer> notificationSetting // key:
                                                                                                    // NotificationTypeCode.name(),
                                                                                                    // value: 0|1
) {
}
