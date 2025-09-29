package com.bukadong.tcg.api.auction.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.time.ZoneOffset;

import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionCloseReason;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.auction.service.dto.WinnerSnapshot;
import com.bukadong.tcg.api.auction.sse.AuctionLiveSseService;
import com.bukadong.tcg.api.auction.util.AuctionDeadlineIndex;
import com.bukadong.tcg.api.bid.service.AuctionCacheService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 경매 종료 서비스
 * <p>
 * 마감 도달 시 낙찰/유찰을 결정하고 후속 처리를 트리거한다.
 * </P>
 *
 * @PARAM auctionId 경매 ID
 * @RETURN 없음
 */
@Service
@RequiredArgsConstructor
public class AuctionFinalizeService {

    private static final Logger log = LoggerFactory.getLogger(AuctionFinalizeService.class);

    private final AuctionRepository auctionRepository;
    private final AuctionWinnerQuery auctionWinnerQuery;
    private final AuctionEventPublisher eventPublisher;
    private static final ZoneOffset UTC = ZoneOffset.UTC;
    private final MemberRepository memberRepository;
    private final AuctionDeadlineIndex deadlineIndex;
    private final AuctionCacheService auctionCacheService;
    private final AuctionLiveSseService auctionLiveSseService;

    /**
     * 경매 종료 처리 (마감 도달 시에만)
     * <p>
     * 입찰 0건이면 UNSOLD(유찰)로 정상 종료한다.
     * </P>
     *
     * @PARAM auctionId 경매 ID
     * @RETURN 없음
     */
    @Transactional
    public void finalizeIfDue(Long auctionId) {
        log.debug("[FinalizeIfDue] start: auctionId={}", auctionId);
        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new IllegalStateException("Auction is not found"));

        if (!auction.isClosableNow()) {
            return; // 아직 마감 시간이 아님
        }

        log.debug("Auction is closed. auctionId={}", auctionId);

        Optional<WinnerSnapshot> winnerOpt = auctionWinnerQuery.tryGetWinnerSnapshot(auctionId);

        // 유찰(입찰 0건) → 정상 종료
        if (winnerOpt.isEmpty()) {
            auction.markClosed(AuctionCloseReason.NO_BIDS, LocalDateTime.now(UTC));
            eventPublisher.publishAuctionUnsold(auctionId);
            afterCommitMarkEndedAndNotify(auctionId);
            afterCommitRemoveIndex(auctionId); // 트랜잭션 커밋 후에 제거
            log.info("Auction closed as UNSOLD (no bids). auctionId={}", auctionId);
            return;
        }

        // 낙찰 처리
        WinnerSnapshot winner = winnerOpt.get();
        auction.setWinner(winner.memberId(), winner.bidId(), winner.amount());
        auction.markClosed(AuctionCloseReason.SOLD, LocalDateTime.now(UTC));

        Member seller = auction.getMember();
        Member buyer = memberRepository.findById(winner.memberId())
                .orElseThrow(() -> new IllegalStateException("Winner member is not found"));

        Instant closedAt = auction.getClosedAt().atZone(UTC).toInstant();
        eventPublisher.publishAuctionSold(auctionId, winner.bidId(), winner.amount(), closedAt, seller, buyer,
                auction.getPhysicalCard());
        afterCommitMarkEndedAndNotify(auctionId);
        afterCommitRemoveIndex(auctionId); // 커밋 성공 후에 제거
        log.info("Auction closed as SOLD. auctionId={}, winner={}, amount={}", auctionId, winner.memberId(),
                winner.amount());
    }

    /* DB 트랜잭션이 성공적으로 커밋된 후에 Redis 인덱스를 제거 */
    private void afterCommitRemoveIndex(Long auctionId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deadlineIndex.remove(auctionId);
            }
        });
    }

    private void afterCommitMarkEndedAndNotify(Long auctionId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    auctionCacheService.markEnded(auctionId);
                } catch (Exception ignore) {
                    // Redis 캐시 마킹 실패는 치명적이지 않음(다음 접근 시 ensureLoaded 또는 워커 보정)
                }
                try {
                    auctionLiveSseService.publishEnded(auctionId);
                } catch (Exception ignore) {
                    // SSE 전파 실패는 일부 클라이언트의 일시 손실일 뿐, 다음 하트비트/새 구독으로 회복
                }
            }
        });
    }
}
