package com.bukadong.tcg.api.bid.service;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionCloseReason;
import com.bukadong.tcg.api.auction.sse.AuctionLiveSseService;
import com.bukadong.tcg.api.auction.util.AuctionDeadlineIndex;
import com.bukadong.tcg.api.bid.entity.AuctionBid;
import com.bukadong.tcg.api.bid.entity.AuctionBidReason;
import com.bukadong.tcg.api.bid.entity.AuctionBidStatus;
import com.bukadong.tcg.api.bid.repository.AuctionBidRepository;
import com.bukadong.tcg.api.notification.service.NotificationCommandService;
import com.bukadong.tcg.api.bid.repository.AuctionLockRepository;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
@Service
@RequiredArgsConstructor
public class BidEventApplyService {

    private static final Logger log = LoggerFactory.getLogger(BidEventApplyService.class);

    private final AuctionLockRepository auctionLockRepository;
    private final AuctionBidRepository auctionBidRepository;
    private final AuctionCacheService auctionCacheService;
    private final ObjectMapper om;
    private final AuctionLiveSseService auctionLiveSseService;
    private final MemberRepository memberRepository;
    private final NotificationCommandService notificationCommandService;
    private final com.bukadong.tcg.api.auction.service.AuctionEventPublisher eventPublisher;

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
        boolean buyNowEvent = (n.hasNonNull("buyNow") && n.get("buyNow").asBoolean())
                || ("BUY_NOW".equalsIgnoreCase(reasonIn));

        // 멱등 처리: 동일 eventId 재실행 방지
        if (auctionBidRepository.existsByEventId(eventId)) {
            log.warn("Skip duplicate bid event: eventId={}, auctionId={}, memberId={}, amount={}", eventId, auctionId,
                    memberId, bidStr);
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
                final Long auctionIdFinal = auctionId;
                final BigDecimal bidFinal = bid;
                final String reasonFinal = mapRejectReason(reasonIn);
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            notificationCommandService.notifyBidRejected(memberId, auctionIdFinal, bidFinal,
                                    reasonFinal);
                        } catch (Exception ex) {
                            log.warn("[afterCommit] notifyBidRejected failed auctionId={}, eventId={}", auctionIdFinal,
                                    eventId, ex);
                        }
                    }
                });
                return;
            }

            // 3) ACCEPT인데 경매가 없으면 영구 실패
            if (auction == null) {
                log.error("ACCEPT event but auction missing: auctionId={}, eventId={}, amount={}", auctionId, eventId,
                        bidStr);
                throw new NonRetryableException("AUCTION_NOT_FOUND:" + auctionId);
            }

            // 4) 정상 ACCEPT 처리
            BigDecimal finalBid = bid;
            if (buyNowEvent && auction.getBuyNowPrice() != null) {
                finalBid = auction.getBuyNowPrice();
            }
            AuctionBid ab = AuctionBid.builder().auction(auction).member(em.getReference(Member.class, memberId))
                    .amount(finalBid).status(AuctionBidStatus.VALID).eventId(eventId).build();
            ab = auctionBidRepository.save(ab);

            // 이전 최고 입찰자/금액 캡처(일반 입찰일 때만 사용)
            Long prevTopBidderId = null;
            BigDecimal prevTopAmount = null;
            if (!buyNowEvent) {
                var prevOpt = auctionBidRepository.findTopByAuctionIdOrderByAmountDescCreatedAtDesc(auctionId);
                if (prevOpt.isPresent()) {
                    var prev = prevOpt.get();
                    if (prev.getMember() != null) {
                        Long pid = prev.getMember().getId();
                        // 본인 재입찰이면 outbid 알림은 생략
                        if (pid != null && !pid.equals(memberId)) {
                            prevTopBidderId = pid;
                            prevTopAmount = prev.getAmount();
                        }
                    }
                }
            }

            if (buyNowEvent) {
                // 즉시구매: 낙찰 처리 (금액은 buy_now_price로 고정하도록 Lua가 amount를 buy_now_price로 반환)
                auction.setWinner(memberId, ab.getId(), finalBid);
                auction.markClosed(AuctionCloseReason.BUY_NOW, LocalDateTime.now(ZoneOffset.UTC));
            } else {
                // 일반 입찰: 현재가 갱신 및 연장 정책
                auction.changeCurrentPrice(finalBid); // 도메인 유효성 검사 포함
            }

            // 부작용(캐시/SSE/ZSET)은 커밋 성공 후 수행하도록 캡처
            final String bidPlain = finalBid.toPlainString();
            final long nowSec = Instant.now().getEpochSecond();
            final String nickname = memberRepository.findById(memberId).map(Member::getNickname)
                    .orElse("member-" + memberId);
            final Long winnerBidIdFinal = buyNowEvent ? ab.getId() : null;
            final Long winnerMemberIdFinal = buyNowEvent ? memberId : null;

            // 연장 관련 사전 계산 (DB endDatetime 은 즉시 변경해도 JPA flush 시 반영)
            final Instant endAtBefore = auction.getEndDatetime() != null
                    ? auction.getEndDatetime().atOffset(ZoneOffset.UTC).toInstant()
                    : null;
            final boolean[] extendedHolder = { false };
            final Instant[] newEndAtHolder = { null };
            if (!buyNowEvent && extensionEnabled && auction.isExtensionFlag() && !auction.isEnd()
                    && auction.getEndDatetime() != null) {
                Instant now = Instant.now();
                Instant endAt = endAtBefore;
                if (endAt != null) {
                    long remainingSec = ChronoUnit.SECONDS.between(now, endAt);
                    if (remainingSec <= extensionThresholdSeconds) {
                        Instant newEndAtInstant = endAt.plusSeconds(extendBySeconds);
                        auction.setEndDatetime(newEndAtInstant.atOffset(ZoneOffset.UTC).toLocalDateTime());
                        extendedHolder[0] = true;
                        newEndAtHolder[0] = newEndAtInstant;
                    }
                }
            }

            // 알림은 트랜잭션 내에서 생성하여 DB 기록을 보장하고, 푸시는 AFTER_COMMIT 리스너가 처리
            final Long createdNotificationId;
            if (!buyNowEvent) {
                Long nid = null;
                try {
                    nid = notificationCommandService.notifyBidAccepted(memberId, auctionId, new BigDecimal(bidPlain));
                } catch (Exception notifyEx) {
                    log.warn("notifyBidAccepted (in-tx) failed auctionId={}, eventId={}", auctionId, eventId, notifyEx);
                }
                createdNotificationId = nid;
            } else {
                createdNotificationId = null;
            }

            // 커밋 이후 실행 등록
            final Long prevTopBidderIdFinal = prevTopBidderId;
            final BigDecimal prevTopAmountFinal = prevTopAmount; // may be null
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        // 가격 캐시 & SSE
                        auctionCacheService.overwritePrice(auctionId, bidPlain);
                        auctionLiveSseService.publishPriceUpdate(auctionId, bidPlain, null);
                        String timeIso = java.time.Instant.ofEpochSecond(nowSec).toString();
                        if (!buyNowEvent) {
                            auctionLiveSseService.publishBidAccepted(auctionId, nickname, bidPlain, timeIso);
                        }
                        // 방금 전에 1위였던 사용자가 있고, 새 입찰 금액이 이전 1위 금액을 초과하면 OUTBID 알림
                        if (!buyNowEvent && prevTopBidderIdFinal != null && prevTopAmountFinal != null
                                && new BigDecimal(bidPlain).compareTo(prevTopAmountFinal) > 0) {
                            try {
                                notificationCommandService.notifyBidOutbid(prevTopBidderIdFinal, auctionId,
                                        new BigDecimal(bidPlain));
                            } catch (Exception outbidEx) {
                                log.warn("[afterCommit] notifyBidOutbid failed auctionId={}, prevTopBidderId={}",
                                        auctionId, prevTopBidderIdFinal, outbidEx);
                            }
                        }
                        // 트랜잭션 내 생성된 알림 ID 로그 (푸시는 AFTER_COMMIT 리스너에서 전송)
                        if (!buyNowEvent) {
                            log.debug(
                                    "[afterCommit] bid-accepted side effects done notificationId={} auctionId={} memberId={} amount={}",
                                    createdNotificationId, auctionId, memberId, bidPlain);
                        }

                        if (buyNowEvent) {
                            // 즉시구매: 종료 처리 후속 작업 (캐시 마킹, SSE 종료, 데드라인 인덱스 제거)
                            try {
                                auctionCacheService.overwritePrice(auctionId, bidPlain);
                                auctionCacheService.markEnded(auctionId);
                                // 즉시구매 전용 이벤트 전파 (상세 구독자)
                                try {
                                    auctionLiveSseService.publishBuyNow(auctionId, nickname, bidPlain,
                                            java.time.Instant.ofEpochSecond(nowSec).toString());
                                } catch (Exception sseBuyNowEx) {
                                    log.warn("[afterCommit] publishBuyNow failed auctionId={}, eventId={}", auctionId,
                                            eventId, sseBuyNowEx);
                                }
                                auctionLiveSseService.publishEnded(auctionId);
                                deadlineIndex.remove(auctionId);
                                // SOLD 이벤트 발행
                                try {
                                    var seller = auction.getMember();
                                    var buyer = memberRepository.findById(winnerMemberIdFinal).orElse(null);
                                    if (buyer != null && winnerBidIdFinal != null) {
                                        Instant closedAt = auction.getClosedAt() != null
                                                ? auction.getClosedAt().atOffset(ZoneOffset.UTC).toInstant()
                                                : Instant.ofEpochSecond(nowSec);
                                        eventPublisher.publishAuctionSold(auctionId, winnerBidIdFinal.longValue(),
                                                new BigDecimal(bidPlain), closedAt, seller, buyer,
                                                auction.getPhysicalCard());
                                        // 즉시구매 사용자/판매자 알림
                                        try {
                                            notificationCommandService.notifyBuyNowBuyer(buyer.getId(), auctionId,
                                                    new BigDecimal(bidPlain), closedAt);
                                        } catch (Exception notifyBuyerEx) {
                                            log.warn("[afterCommit] notifyBuyNowBuyer failed auctionId={}, eventId={}",
                                                    auctionId, eventId, notifyBuyerEx);
                                        }
                                        try {
                                            if (seller != null) {
                                                notificationCommandService.notifyBuyNowSeller(seller.getId(), auctionId,
                                                        new BigDecimal(bidPlain), closedAt);
                                            }
                                        } catch (Exception notifySellerEx) {
                                            log.warn("[afterCommit] notifyBuyNowSeller failed auctionId={}, eventId={}",
                                                    auctionId, eventId, notifySellerEx);
                                        }
                                    }
                                } catch (Exception pubEx) {
                                    log.warn("[afterCommit] publishAuctionSold failed auctionId={}, eventId={}",
                                            auctionId, eventId, pubEx);
                                }
                            } catch (Exception side2) {
                                log.warn("[afterCommit] buy-now side effects failed auctionId={}, eventId={}",
                                        auctionId, eventId, side2);
                            }
                        } else {
                            // 마감 연장/등록 처리
                            try {
                                if (extendedHolder[0] && newEndAtHolder[0] != null) {
                                    long newEndSec = newEndAtHolder[0].getEpochSecond();
                                    auctionCacheService.reopenUntil(auctionId, newEndSec);
                                    auctionLiveSseService.publishEndTsUpdate(auctionId, newEndSec);
                                    deadlineIndex.upsert(auctionId, newEndAtHolder[0].toEpochMilli());
                                    log.debug("[afterCommit] Auction end extended: auctionId={}, new={}", auctionId,
                                            newEndAtHolder[0]);
                                } else if (endAtBefore != null) {
                                    deadlineIndex.upsert(auctionId, endAtBefore.toEpochMilli());
                                }
                            } catch (Exception schedEx) {
                                log.error("[afterCommit] Failed deadline scheduling update auctionId={}, eventId={}",
                                        auctionId, eventId, schedEx);
                            }
                        }
                    } catch (Exception sideEx) {
                        // 커밋 후 부작용 실패는 워커/리컨실/주기적 보정으로 회복 (로그만 남김)
                        log.error("[afterCommit] Side effect error auctionId={}, eventId={}", auctionId, eventId,
                                sideEx);
                    }
                }
            });

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
            // 예기치 못한 오류: 우선 재시도(일시 환경 요인 가능). 재시도 후에도 반복되면 운영介入.
            throw new RetryableException("UNEXPECTED_TRANSIENT?", ex);
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
            // 실패 레코드가 실제 커밋될 때 알림 (커밋 보장하려면 상위 트랜잭션이 롤백되지 않아야 함)
            final Long auctionIdFinal = auctionId;
            final BigDecimal bidFinal = bid;
            final String codeFinal = code;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        notificationCommandService.notifyBidFailed(memberId, auctionIdFinal, bidFinal, codeFinal);
                    } catch (Exception ex) {
                        log.warn("[afterCommit] notifyBidFailed failed auctionId={}, eventId={}", auctionIdFinal,
                                eventId, ex);
                    }
                }
            });
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
