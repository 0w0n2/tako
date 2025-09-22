package com.bukadong.tcg.api.member.dto.request;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "UpdateMyProfileRequest", description = "내 프로필 수정 요청. null 필드는 변경하지 않음")
public record UpdateMyProfileRequest(
        @Schema(description = "닉네임(2~10자)", example = "타코수집가") @Size(min = 2, max = 10) String nickname,
        @Schema(description = "자기소개(최대 255자)", example = "반갑습니다. 수집 좋아해요.") @Size(max = 255) String introduction,
        @Schema(description = "알림 설정: code별 0/1", example = "{\"WISH_AUCTION_STARTED\":1,"
                + "\"WISH_AUCTION_DUE_SOON\":1," + "\"WISH_AUCTION_ENDED\":1," + "\"WISH_CARD_LISTED\":1,"
                + "\"AUCTION_NEW_INQUIRY\":1," + "\"INQUIRY_ANSWERED\":1," + "\"AUCTION_WON\":1,"
                + "\"AUCTION_CLOSED_SELLER\":1," + "\"AUCTION_CANCELED\":1}") Map<String, Integer> notificationSetting // key:
// NotificationTypeCode.name(),
// value:
// 0|1,
// null이면
// 수정안함
) {
}
