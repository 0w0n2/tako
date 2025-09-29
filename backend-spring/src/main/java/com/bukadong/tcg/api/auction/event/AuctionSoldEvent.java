package com.bukadong.tcg.api.auction.event;

import com.bukadong.tcg.api.card.entity.PhysicalCard;
import com.bukadong.tcg.api.member.entity.Member;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 경매 종료 이벤트
 * <p>
 * 낙찰자/금액/시각을 포함하며, 결과 저장 및 알림 발송 리스너가 구독한다.
 * </P>
 *
 * @PARAM auctionId 경매 ID
 * @PARAM winnerId 낙찰자 ID
 * @PARAM amount 낙찰 금액
 * @PARAM bidId 최종 입찰 ID
 * @PARAM closedAt 종료 시각
 * @RETURN 없음
 */
public record AuctionSoldEvent(
        Long auctionId,
        Long bidId,
        BigDecimal amount,
        Instant closedAt,
        Member seller,
        Member buyer,
        PhysicalCard physicalCard
) {
}
