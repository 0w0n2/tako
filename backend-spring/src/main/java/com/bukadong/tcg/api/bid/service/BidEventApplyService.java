package com.bukadong.tcg.api.bid.service;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.bid.entity.AuctionBid;
import com.bukadong.tcg.api.bid.entity.AuctionBidReason;
import com.bukadong.tcg.api.bid.entity.AuctionBidStatus;
import com.bukadong.tcg.api.bid.repository.AuctionBidRepository;
import com.bukadong.tcg.api.bid.repository.AuctionLockRepository;
import com.bukadong.tcg.api.member.entity.Member;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final ObjectMapper om;

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
            throw new NonRetryableException("BAD_JSON", e);
        }

        // 필수 필드 검증
        if (!n.hasNonNull("auctionId") || !n.hasNonNull("memberId") || !n.hasNonNull("bidPrice")
                || !n.hasNonNull("eventId")) {
            throw new NonRetryableException("BAD_PAYLOAD_FIELDS");
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
            // ACCEPT 이벤트인데 경매가 없으면 DLQ
            if (auction == null) {
                log.error("ACCEPT event but auction missing: auctionId={}, eventId={}", auctionId, eventId);
                throw new NonRetryableException("AUCTION_NOT_FOUND:" + auctionId);
            }

            // 정상처리 ACCEPT
            AuctionBid ab = AuctionBid.builder().auction(auction)
                    .member(em.getReference(com.bukadong.tcg.api.member.entity.Member.class, memberId)).bidPrice(bid)
                    .status(AuctionBidStatus.VALID).eventId(eventId).build();
            auctionBidRepository.save(ab);

            auction.changeCurrentPrice(bid); // DB 현재가 갱신
            auctionCacheService.overwritePrice(auctionId, bid.toPlainString()); // Redis 캐시 보정(상승만 반영)
        } catch (PessimisticLockException | LockAcquisitionException e) {
            // 락 경합 등 일시 오류 → 재시도
            throw new RetryableException("LOCK_CONTENTION", e);
        } catch (TransientDataAccessException e) {
            // 일시적 DB 접근 오류 → 재시도
            throw new RetryableException("TRANSIENT_DB_ERROR", e);
        } catch (DataIntegrityViolationException | PersistenceException e) {
            // 제약 위반/영구 오류 → FAILED 기록 시도 후 DLQ
            safelyRecordFailed(auctionId, memberId, bid, eventId, "DB_CONSTRAINT");
            throw new NonRetryableException("DB_CONSTRAINT", e);
        } catch (NonRetryableException non) {
            // 위에서 명시적으로 분류된 영구 실패
            throw non;
        } catch (Exception ex) {
            // 예기치 못한 오류 → FAILED 기록 시도 후 DLQ
            safelyRecordFailed(auctionId, memberId, bid, eventId, "UNEXPECTED");
            throw new NonRetryableException("UNEXPECTED", ex);
        }
    }

    /**
     * FAILED 레코드 안전 기록 (절대 2차 예외 던지지 않음)
     */
    private void safelyRecordFailed(Long auctionId, Long memberId, BigDecimal bid, String eventId, String code) {
        try {
            if (auctionBidRepository.existsByEventId(eventId))
                return;
            Auction a = em.find(Auction.class, auctionId);
            if (a == null)
                return; // 부모 FK 보호
            Member m = em.find(Member.class, memberId);
            if (m == null)
                return; // member FK 보호(컬럼이 NOT NULL일 수 있으므로 스킵)
            auctionBidRepository.save(AuctionBid.builder().auction(a).member(m).bidPrice(bid)
                    .status(AuctionBidStatus.FAILED).reasonCode(code).eventId(eventId).build());
        } catch (Exception ignore) {
            // 기록 실패도 무시 (로그 노이즈/2차 실패 방지)
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
            return AuctionBidReason.REJECTED.name();
        switch (r) {
        case "LOW_PRICE":
            return AuctionBidReason.LOW_PRICE.name();
        case "NOT_RUNNING":
            return AuctionBidReason.NOT_RUNNING.name();
        case "MISSING":
            return AuctionBidReason.MISSING.name();
        case "PRECHECK":
            return AuctionBidReason.PRECHECK.name();
        case "SELF_BID":
            return AuctionBidReason.SELF_BID.name();
        default:
            return r;
        }
    }
}
