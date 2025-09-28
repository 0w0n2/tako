package com.bukadong.tcg.api.notification.util;

import com.bukadong.tcg.api.notification.entity.NotificationTypeCode;
import java.util.EnumMap;
import java.util.Map;

/**
 * Service Worker 가 기대하는 data.* 필드 규칙을 서버 NotificationTypeCode 와 매핑하기 위한 헬퍼.
 * <p>
 * 1) type : SW 로 내려보낼 최상위 분류 (예: WISH, AUCTION, BID, DELIVERY, INQUIRY) 2) tag
 * : 중복 도착 시 동일 tag 는 브라우저에서 덮어쓰게 하여 노이즈 감소. 기본: type + "-" + causeId 3)
 * click_action : targetUrl (Notification 엔티티 저장 시 생성) 활용.
 * </p>
 */
public final class NotificationPushPayloadMapper {

    private NotificationPushPayloadMapper() {
    }

    /** NotificationTypeCode -> 상위 type 그룹 */
    private static final String WISH = "WISH";
    private static final String INQUIRY = "INQUIRY";
    private static final String AUCTION = "AUCTION";
    private static final String BID = "BID";
    private static final String DELIVERY = "DELIVERY";

    private static final Map<NotificationTypeCode, String> GROUP_MAP = new EnumMap<>(NotificationTypeCode.class);
    static {
        // 위시 관련
        GROUP_MAP.put(NotificationTypeCode.WISH_AUCTION_STARTED, WISH);
        GROUP_MAP.put(NotificationTypeCode.WISH_AUCTION_DUE_SOON, WISH);
        GROUP_MAP.put(NotificationTypeCode.WISH_AUCTION_ENDED, WISH);
        GROUP_MAP.put(NotificationTypeCode.WISH_CARD_LISTED, WISH);

        // 문의 / 경매 내 활동
        GROUP_MAP.put(NotificationTypeCode.AUCTION_NEW_INQUIRY, INQUIRY);
        GROUP_MAP.put(NotificationTypeCode.INQUIRY_ANSWERED, INQUIRY);

        // 경매 상태/결과
        GROUP_MAP.put(NotificationTypeCode.AUCTION_WON, AUCTION);
        GROUP_MAP.put(NotificationTypeCode.AUCTION_CLOSED_SELLER, AUCTION);
        GROUP_MAP.put(NotificationTypeCode.AUCTION_CANCELED, AUCTION);

        // 공지
        GROUP_MAP.put(NotificationTypeCode.NOTICE_NEW, "NOTICE");

        // 입찰
        GROUP_MAP.put(NotificationTypeCode.BID_ACCEPTED, BID);
        GROUP_MAP.put(NotificationTypeCode.BID_REJECTED, BID);
        GROUP_MAP.put(NotificationTypeCode.BID_FAILED, BID);
        GROUP_MAP.put(NotificationTypeCode.BID_OUTBID, BID);

        // 배송
        GROUP_MAP.put(NotificationTypeCode.DELIVERY_STARTED, DELIVERY);
        GROUP_MAP.put(NotificationTypeCode.DELIVERY_STATUS_CHANGED, DELIVERY);
        GROUP_MAP.put(NotificationTypeCode.DELIVERY_CONFIRM_REQUEST, DELIVERY);
        GROUP_MAP.put(NotificationTypeCode.DELIVERY_CONFIRMED_SELLER, DELIVERY);
    }

    public static String mapGroup(NotificationTypeCode code) {
        return GROUP_MAP.getOrDefault(code, "GENERIC");
    }

    /**
     * 기본 tag 정책: group + '-' + (causeId 존재 시 causeId) 없으면 group.
     */
    public static String buildTag(NotificationTypeCode code, Long causeId) {
        String g = mapGroup(code);
        return causeId == null ? g : g + "-" + causeId;
    }
}
