package com.bukadong.tcg.api.auction.service;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.bukadong.tcg.api.auction.event.AuctionSoldEvent;
import com.bukadong.tcg.api.auction.event.AuctionUnsoldEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 경매 이벤트 퍼블리셔 기본 구현
 * <P>
 * Spring ApplicationEvent 또는 향후 Kafka 등으로 교체 가능.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionEventPublisherImpl implements AuctionEventPublisher {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publishAuctionSold(long auctionId, long winnerId, long bidId, BigDecimal amount, Instant closedAt) {
        publisher.publishEvent(new AuctionSoldEvent(auctionId, winnerId, amount, bidId, closedAt));
        log.info("[Event] SOLD auctionId={}, winnerId={}, bidId={}, amount={}", auctionId, winnerId, bidId, amount);
    }

    @Override
    public void publishAuctionUnsold(long auctionId) {
        publisher.publishEvent(new AuctionUnsoldEvent(auctionId));
        log.info("[Event] UNSOLD auctionId={}", auctionId);
    }
}
