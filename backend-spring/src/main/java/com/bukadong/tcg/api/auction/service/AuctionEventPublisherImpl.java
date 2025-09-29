package com.bukadong.tcg.api.auction.service;

import java.math.BigDecimal;
import java.time.Instant;

import com.bukadong.tcg.api.card.entity.PhysicalCard;
import com.bukadong.tcg.api.member.entity.Member;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.bukadong.tcg.api.auction.event.AuctionSoldEvent;
import com.bukadong.tcg.api.auction.event.AuctionUnsoldEvent;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 경매 이벤트 퍼블리셔 기본 구현
 * <P>
 * Spring ApplicationEvent 또는 향후 Kafka 등으로 교체 가능.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Service
@RequiredArgsConstructor
public class AuctionEventPublisherImpl implements AuctionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AuctionEventPublisherImpl.class);

    private final ApplicationEventPublisher publisher;

    @Override
    public void publishAuctionSold(long auctionId, long bidId, BigDecimal amount, Instant closedAt, Member seller,
            Member buyer, PhysicalCard physicalCard) {
        publisher.publishEvent(new AuctionSoldEvent(auctionId, bidId, amount, closedAt, seller, buyer, physicalCard));
        log.info("[Event] SOLD auctionId={}, winnerId={}, bidId={}, amount={}", auctionId, buyer.getId(), bidId,
                amount);
    }

    @Override
    public void publishAuctionUnsold(long auctionId) {
        publisher.publishEvent(new AuctionUnsoldEvent(auctionId));
        log.info("[Event] UNSOLD auctionId={}", auctionId);
    }
}
