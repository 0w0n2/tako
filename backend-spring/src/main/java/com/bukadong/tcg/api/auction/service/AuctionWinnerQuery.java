package com.bukadong.tcg.api.auction.service;

import java.math.BigDecimal;

/**
 * 낙찰 정보 조회 인터페이스
 * <P>
 * 최고 입찰 1건(금액 같으면 최신 시간)을 조회하여 종료 이벤트 페이로드로 제공한다.
 * </P>
 * 
 * @PARAM auctionId 경매 ID
 * @RETURN WinnerSnapshot(멤버ID/금액/입찰ID)
 */
public interface AuctionWinnerQuery {
    record WinnerSnapshot(long memberId, BigDecimal amount, long bidId) {
    }

    WinnerSnapshot getWinnerSnapshot(long auctionId);
}
