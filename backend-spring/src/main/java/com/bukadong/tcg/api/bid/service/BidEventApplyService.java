package com.bukadong.tcg.api.bid.service;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.bid.entity.AuctionBid;
import com.bukadong.tcg.api.bid.entity.AuctionBidStatus;
import com.bukadong.tcg.api.bid.repository.AuctionBidRepository;
import com.bukadong.tcg.api.bid.repository.AuctionLockRepository;
import com.bukadong.tcg.api.member.entity.Member;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 입찰 이벤트 DB 반영 서비스
 * <P>
 * 큐에서 꺼낸 단일 이벤트를 트랜잭션으로 처리한다. 행락(SELECT ... FOR UPDATE)으로 현재가 갱신과 입찰 레코드 삽입의
 * 원자성을 보장한다. 일시 실패는 RetryableException으로 전파하여 컨슈머가 재시도 큐로 라우팅하도록 한다.
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
     * 행락으로 원자성 보장. 일시 실패는 RetryableException으로 전파.
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
            // 포맷 오류는 영구 실패 → 버림(사망 큐로 보낼 필요 없음)
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
        if (auctionBidRepository.existsByEventId(eventId)) {
            return;
        }

        BigDecimal bid = new BigDecimal(bidStr);

        try {
            // 행락
            Auction auction = auctionLockRepository.findByIdForUpdate(auctionId).orElse(null);

            if ("REJECT".equalsIgnoreCase(intended)) {
                if (auction == null) {
                    log.warn("Reject event for missing auction: auctionId={}, eventId={}, reason={}", auctionId,
                            eventId, reasonIn);
                    return;
                }
                AuctionBid ab = AuctionBid.builder().auction(auction)
                        .member(em.getReference(com.bukadong.tcg.api.member.entity.Member.class, memberId))
                        .bidPrice(bid).status(AuctionBidStatus.REJECTED).eventId(eventId)
                        .reasonCode(mapRejectReason(reasonIn)).build();
                auctionBidRepository.save(ab);
                return;
            }
            // 2) ACCEPT 이벤트인데 경매가 없으면 → 비재시도(dead-letter)
            if (auction == null) {
                log.error("ACCEPT event but auction missing: auctionId={}, eventId={}", auctionId, eventId);
                throw new NonRetryableException("AUCTION_NOT_FOUND:" + auctionId);
            }

            // 정상처리 ACCEPT
            AuctionBid ab = AuctionBid.builder().auction(auction)
                    .member(em.getReference(com.bukadong.tcg.api.member.entity.Member.class, memberId)).bidPrice(bid)
                    .status(AuctionBidStatus.VALID).eventId(eventId).build();
            auctionBidRepository.save(ab);

            auction.changeCurrentPrice(bid);
            auctionCacheService.overwritePrice(auctionId, bid.toPlainString());

        } catch (PessimisticLockException | LockAcquisitionException e) {
            // 락 경합 등 일시 실패 → 재시도
            throw new RetryableException("Lock contention", e);
        } catch (TransientDataAccessException e) {
            // 일시적 데이터 접근 오류 → 재시도
            throw new RetryableException("Transient DB error", e);
        } catch (NonRetryableException non) {
            throw non; // 그대로 전파 → dead-letter
        } catch (Exception ex) {
            // 기타 예외: 경매가 실제 존재할 때만 FAILED 기록(없으면 FK 위반 위험)
            log.error("Bid DB apply failed, eventId={}, err={}", eventId, ex.toString());
            if (!auctionBidRepository.existsByEventId(eventId)) {
                Auction auctionExists = em.find(Auction.class, auctionId);
                if (auctionExists != null) {
                    AuctionBid fail = AuctionBid.builder().auction(auctionExists)
                            .member(em.getReference(Member.class, memberId)).bidPrice(bid)
                            .status(AuctionBidStatus.FAILED).eventId(eventId).reasonCode("DB_ERROR").build();
                    auctionBidRepository.save(fail);
                } else {
                    // 경매 미존재면 기록 생략(죽은 부모 FK 방지)
                    log.warn("Skip FAILED record because auction missing: auctionId={}, eventId={}", auctionId,
                            eventId);
                }
            }
            // 영구 실패로 간주 → dead-letter
            throw new NonRetryableException("UNEXPECTED_ERROR:" + ex.getClass().getSimpleName(), ex);
        }
    }

    /**
     * 거절 사유 매핑
     * <P>
     * 입력 문자열을 표준 코드로 매핑한다.
     * </P>
     * 
     * @PARAM r 입력 사유
     * @RETURN 표준화된 사유 코드
     */
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
        case "SELF_BID":
            return "SELF_BID";
        default:
            return r;
        }
    }
}
