package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.card.entity.PhysicalCard;
import com.bukadong.tcg.api.member.entity.Member;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 경매 이벤트 퍼블리셔
 * <P>
 * 낙찰/유찰 등 도메인 이벤트를 발행한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public interface AuctionEventPublisher {
    void publishAuctionSold(long auctionId, long bidId, BigDecimal amount, Instant closedAt, Member seller, Member buyer, PhysicalCard physicalCard);

    void publishAuctionUnsold(long auctionId); // 유찰은 결과 저장 안 함(이벤트만 로그)
}
