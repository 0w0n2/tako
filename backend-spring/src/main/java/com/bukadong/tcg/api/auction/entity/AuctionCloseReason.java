package com.bukadong.tcg.api.auction.entity;

/**
 * 경매 종료 사유
 * <P>
 * 정상 낙찰/유찰/취소 등 종료 원인을 구분한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public enum AuctionCloseReason {
    SOLD, // 낙찰
    NO_BIDS, // 입찰 없음(유찰)
    CANCELED, // 관리자 취소
    EXPIRED_ERROR, // 비정상 만료(보전/조사)
    DUE_TIME, // 마감 시각 도래
    BUY_NOW, // 즉시구매로 종료
    ADMIN_CANCEL, // 관리자 취소
    SELLER_CANCEL, // 판매자 취소
    SYSTEM_ERROR // 시스템 장애로 강제 종료
}
