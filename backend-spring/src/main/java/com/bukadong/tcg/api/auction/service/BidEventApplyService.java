package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionBid;
import com.bukadong.tcg.api.auction.entity.AuctionBidStatus;
import com.bukadong.tcg.api.auction.repository.AuctionBidRepository;
import com.bukadong.tcg.api.auction.repository.AuctionLockRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 입찰 이벤트 DB 반영 서비스
 * <P>
 * 큐에서 꺼낸 단일 이벤트를 트랜잭션으로 처리한다. 컨슈머에서 호출한다.
 * </P>
 * 
 * @PARAM json Redis 큐에서 팝한 이벤트 JSON
 * @RETURN 없음
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BidEventApplyService {

    private final AuctionLockRepository auctionLockRepository;
    private final AuctionBidRepository auctionBidRepository;
    private final AuctionCacheService auctionCacheService;
    private final ObjectMapper om = new ObjectMapper();

    @PersistenceContext
    private EntityManager em;

    /**
     * 이벤트 1건 처리 (트랜잭션)
     * <P>
     * 행락(SELECT ... FOR UPDATE)으로 현재가 갱신과 입찰 레코드 삽입의 원자성을 보장한다.
     * </P>
     * 
     * @PARAM json 이벤트 JSON
     * @RETURN 없음
     */
    @Transactional
    public void applyEvent(String json) {
        JsonNode n;
        try {
            n = om.readTree(json);
        } catch (Exception e) {
            log.error("JSON parsing failed: {}", json, e);
            return;
        }

        Long auctionId = n.get("auctionId").asLong();
        Long memberId = n.get("memberId").asLong();
        String bidStr = n.get("bidPrice").asText();
        String eventId = n.get("eventId").asText();
        String intended = n.hasNonNull("intended") ? n.get("intended").asText() : "ACCEPT";
        String reasonIn = n.hasNonNull("reason") ? n.get("reason").asText() : null;

        // 멱등 처리
        if (auctionBidRepository.existsByEventId(eventId))
            return;

        BigDecimal bid = new BigDecimal(bidStr);

        // 행락
        Auction auction = auctionLockRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new IllegalStateException("auction not found: " + auctionId));

        try {
            if ("REJECT".equalsIgnoreCase(intended)) {
                AuctionBid ab = AuctionBid.builder().auction(auction)
                        .member(em.getReference(com.bukadong.tcg.api.member.entity.Member.class, memberId))
                        .bidPrice(bid).status(AuctionBidStatus.REJECTED).eventId(eventId)
                        .reasonCode(mapRejectReason(reasonIn)).build();
                auctionBidRepository.save(ab);
                return;
            }

            // ACCEPT
            AuctionBid ab = AuctionBid.builder().auction(auction)
                    .member(em.getReference(com.bukadong.tcg.api.member.entity.Member.class, memberId)).bidPrice(bid)
                    .status(AuctionBidStatus.VALID).eventId(eventId).build();
            auctionBidRepository.save(ab);

            auction.changeCurrentPrice(bid);
            auctionCacheService.overwritePrice(auctionId, bid.toPlainString());

        } catch (Exception ex) {
            log.error("Bid DB apply failed, eventId={}, err={}", eventId, ex.toString());
            if (!auctionBidRepository.existsByEventId(eventId)) {
                AuctionBid fail = AuctionBid.builder().auction(auction)
                        .member(em.getReference(com.bukadong.tcg.api.member.entity.Member.class, memberId))
                        .bidPrice(bid).status(AuctionBidStatus.FAILED).eventId(eventId).reasonCode("DB_ERROR").build();
                auctionBidRepository.save(fail);
            }
        }
    }

    private String mapRejectReason(String r) {
        if (r == null)
            return "REJECTED";
        switch (r) {
        case "LOW_PRICE":
            return "LOW_PRICE";
        case "NOT_RUNNING":
            return "NOT_RUNNING";
        case "MISSING":
            return "MISSING";
        case "PRECHECK":
            return "PRECHECK";
        default:
            return r;
        }
    }
}
