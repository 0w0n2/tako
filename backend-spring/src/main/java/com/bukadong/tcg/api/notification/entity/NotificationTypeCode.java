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

    // 입찰 결과 (비동기 반영)
    BID_ACCEPTED, // 입찰 반영 성공
    BID_REJECTED, // 입찰 거절 (가격/기간 등 조건 미충족)
    BID_FAILED, // 시스템 오류로 반영 실패 (옵션)
    BID_OUTBID, // 상위 입찰 발생으로 최고가 지위 상실

    // 배송
    DELIVERY_STARTED, // 운송장 등록 등으로 배송 시작됨(구매자)
    DELIVERY_STATUS_CHANGED, // 배송 상태 변경 알림(구매자)
    DELIVERY_CONFIRM_REQUEST, // 배송 완료 시 구매 확정 요청(구매자)
    DELIVERY_CONFIRMED_SELLER, // 구매자가 확정 완료(판매자)

    // 판매자 알림 추가
    AUCTION_CLOSED_SELLER,

    // 경매 취소
    AUCTION_CANCELED,

    // 공지 (신규: 전체 또는 특정 그룹 공지 브로드캐스트)
    NOTICE_NEW
}
