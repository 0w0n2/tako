package com.bukadong.tcg.api.auction.service;

import java.math.BigDecimal;

/**
 * 경매 정산 서비스
 * <P>
 * 낙찰 후 금액 정산 큐에 적재.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public interface AuctionSettlementService {
    void enqueue(Long auctionId, Long winnerMemberId, BigDecimal amount);

    void createEscrowForAuction(Long auctionId, Long winnerMemberId, BigDecimal amount);
}
