package com.bukadong.tcg.api.member.dto.request;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdateNotificationSettingRequest", description = "푸시(Notification) 설정 변경 요청")
public record UpdateNotificationSettingRequest(
        @Schema(description = "알림 설정: code별 0/1", example = "{\"DELIVERY_CONFIRM_REQUEST\":1,\"INQUIRY_ANSWERED\":1,\"BID_FAILED\":1,\"AUCTION_CLOSED_SELLER\":1,\"AUCTION_NEW_INQUIRY\":1,\"AUCTION_CANCELED\":1,\"BID_REJECTED\":1,\"NOTICE_NEW\":1,\"WISH_CARD_LISTED\":1,\"WISH_AUCTION_STARTED\":1,\"DELIVERY_STATUS_CHANGED\":1,\"DELIVERY_CONFIRMED_SELLER\":1,\"DELIVERY_STARTED\":1,\"BID_ACCEPTED\":1,\"AUCTION_WON\":1,\"WISH_AUCTION_DUE_SOON\":1,\"WISH_AUCTION_ENDED\":1}") @NotNull Map<String, Integer> notificationSetting) {
}
