package com.bukadong.tcg.api.notification.entity;

/**
 * 알림 타입 코드
 * <P>
 * DB notification_type.code에 매핑되는 상수 집합. 코드 문자열은 DB seed와 동일해야 한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public enum NotificationTypeCode {
    // 위시 경매
    WISH_AUCTION_STARTED, WISH_AUCTION_DUE_SOON, WISH_AUCTION_ENDED,

    // 위시 카드
    WISH_CARD_LISTED,

    // 문의
    AUCTION_NEW_INQUIRY, INQUIRY_ANSWERED,

    // 경매
    AUCTION_WON,

    // 배송
    DELIVERY_STARTED, // 운송장 등록 등으로 배송 시작됨(구매자)
    DELIVERY_STATUS_CHANGED, // 배송 상태 변경 알림(구매자)
    DELIVERY_CONFIRM_REQUEST, // 배송 완료 시 구매 확정 요청(구매자)
    DELIVERY_CONFIRMED_SELLER, // 구매자가 확정 완료(판매자)

    // 판매자 알림 추가
    AUCTION_CLOSED_SELLER,

    // 경매 취소
    AUCTION_CANCELED
}
