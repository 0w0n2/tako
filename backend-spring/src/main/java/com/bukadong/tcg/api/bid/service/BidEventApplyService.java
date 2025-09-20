package com.bukadong.tcg.api.bid.service;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.util.AuctionDeadlineIndex;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * 입찰 이벤트 DB 반영 서비스
 * <P>
 * 큐에서 꺼낸 단일 이벤트를 트랜잭션으로 처리한다. 행락(SELECT ... FOR UPDATE)으로 현재가 갱신과 입찰 레코드 삽입의
 * 원자성을 보장한다. 입찰 성공 시, 연장 정책에 따라 마감 시각을 조정하고 Redis ZSET(DEADLINES)에 스케줄을 반영한다.
 * 일시 실패는 RetryableException으로 전파하여 컨슈머가 재시도 큐로 라우팅하도록 한다.
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

    /** 데드라인 스케줄 반영용 Redis */
    private final StringRedisTemplate stringRedisTemplate;
    private final AuctionDeadlineIndex deadlineIndex;
    /** 연장 기능 on/off */
    @Value("${auction.extension.enabled:true}")
    private boolean extensionEnabled;

    /** 연장 임계(남은 시간 <= threshold면 연장) */
    @Value("${auction.extension.threshold-seconds:60}")
    private long extensionThresholdSeconds;

    /** 연장 기간(초) */
    @Value("${auction.extension.extend-by-seconds:60}")
    private long extendBySeconds;

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
        if (!n.hasNonNull("auctionId") || !n.hasNonNull("memberId") || !n.hasNonNull("amount")
                || !n.hasNonNull("eventId")) {
            throw new NonRetryableException("BAD_PAYLOAD_FIELDS");
        }

        Long auctionId = n.get("auctionId").asLong();
        Long memberId = n.get("memberId").asLong();
        String bidStr = n.get("amount").asText();
        String eventId = n.get("eventId").asText();
        String intended = n.hasNonNull("intended") ? n.get("intended").asText() : "ACCEPT";
        String reasonIn = n.hasNonNull("reason") ? n.get("reason").asText() : null;

        // 멱등 처리: 동일 eventId 재실행 방지
        if (auctionBidRepository.existsByEventId(eventId)) {
            return;
        }

        BigDecimal bid = new BigDecimal(bidStr);

        try {
            // 1) 행락(SELECT ... FOR UPDATE)으로 경매 행 잠금
            Auction auction = auctionLockRepository.findByIdForUpdate(auctionId).orElse(null);

            // 2) REJECT 이벤트 처리 (존재하지 않는 경매면 기록만 스킵)
            if ("REJECT".equalsIgnoreCase(intended)) {
                if (auction == null) {
                    log.warn("Reject event for missing auction: auctionId={}, eventId={}, reason={}", auctionId,
                            eventId, reasonIn);
                    return;
                }
                AuctionBid ab = AuctionBid.builder().auction(auction).member(em.getReference(Member.class, memberId))
                        .amount(bid).status(AuctionBidStatus.REJECTED).eventId(eventId)
                        .reasonCode(mapRejectReason(reasonIn)).build();
                auctionBidRepository.save(ab);
                return;
            }

            // 3) ACCEPT인데 경매가 없으면 영구 실패
            if (auction == null) {
                log.error("ACCEPT event but auction missing: auctionId={}, eventId={}", auctionId, eventId);
                throw new NonRetryableException("AUCTION_NOT_FOUND:" + auctionId);
            }

            // 4) 정상 ACCEPT 처리: 입찰 저장
            AuctionBid ab = AuctionBid.builder().auction(auction).member(em.getReference(Member.class, memberId))
                    .amount(bid).status(AuctionBidStatus.VALID).eventId(eventId).build();
            auctionBidRepository.save(ab);

            // 5) DB 현재가 갱신
            auction.changeCurrentPrice(bid);

            // 6) Redis 가격 캐시 보정(상승만 반영)
            auctionCacheService.overwritePrice(auctionId, bid.toPlainString());

            // 7) (핵심) 마감 연장 & 데드라인 ZSET 갱신
            // - 같은 트랜잭션 맥락에서 endAt 변경 및 ZSET 스코어 갱신
            try {
                if (extensionEnabled && auction.isExtensionFlag() && !auction.isEnd()
                        && auction.getEndDatetime() != null) {
                    Instant now = Instant.now();
                    Instant endAt = auction.getEndDatetime().atZone(ZoneId.systemDefault()).toInstant();
                    long remainingSec = ChronoUnit.SECONDS.between(now, endAt);

                    if (remainingSec <= extensionThresholdSeconds) {
                        // 임계 이하 → 연장
                        Instant newEndAt = endAt.plusSeconds(extendBySeconds);

                        // (a) DB 엔티티 반영(도메인 메서드 사용)
                        auction.setEndDatetime(newEndAt.atZone(ZoneId.systemDefault()).toLocalDateTime());

                        // (b) Redis ZSET 스코어 갱신(epochMillis)
                        deadlineIndex.upsert(auctionId, newEndAt.toEpochMilli());

                        log.debug("Auction end extended: auctionId={}, old={}, new={}", auctionId, endAt, newEndAt);
                    } else {
                        // 연장 조건 미충족 → 그래도 ZSET에 보장 등록
                        deadlineIndex.upsert(auctionId, endAt.toEpochMilli());
                    }
                } else if (auction.getEndDatetime() != null) {
                    // 연장 기능 OFF이거나 확장 비대상이어도, 최소 1회 ZSET 등록은 보장
                    Instant endAt = auction.getEndDatetime().atZone(ZoneId.systemDefault()).toInstant();
                    deadlineIndex.upsert(auctionId, endAt.toEpochMilli());
                }
            } catch (Exception schedEx) {
                // 스케줄 갱신 실패는 입찰 자체 실패로 만들지 않음 (워커/리컨실 보정으로 회복 가능)
                log.error("Failed to update deadline scheduling. auctionId={}, eventId={}", auctionId, eventId,
                        schedEx);
            }

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
     * <P>
     * 부모 FK 보호를 위해 auction/member 존재 시에만 기록한다.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @PARAM memberId 멤버 ID
     * @PARAM bid 입찰가
     * @PARAM eventId 이벤트 ID(멱등키)
     * @PARAM code 실패 코드
     * @RETURN 없음
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
                return; // member FK 보호(컬럼 NOT NULL일 수 있으므로 스킵)

            auctionBidRepository.save(AuctionBid.builder().auction(a).member(m).amount(bid)
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
