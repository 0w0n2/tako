package com.bukadong.tcg.api.auction.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionCloseReason;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.auction.service.dto.WinnerSnapshot;
import com.bukadong.tcg.api.auction.util.AuctionDeadlineIndex;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 경매 종료 서비스
 * <p>
 * 마감 도달 시 낙찰/유찰을 결정하고 후속 처리를 트리거한다.
 * </P>
 *
 * @PARAM auctionId 경매 ID
 * @RETURN 없음
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionFinalizeService {

    private final AuctionRepository auctionRepository;
    private final AuctionWinnerQuery auctionWinnerQuery;
    private final AuctionSettlementService settlementService;
    private final AuctionEventPublisher eventPublisher;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final AuctionDeadlineIndex deadlineIndex;

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
        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        if (!auction.isClosableNow()) {
            return; // 아직 마감 시간이 아님
        }

        Optional<WinnerSnapshot> winnerOpt = auctionWinnerQuery.tryGetWinnerSnapshot(auctionId);

        // 유찰(입찰 0건) → 정상 종료
        if (winnerOpt.isEmpty()) {
            auction.markClosed(AuctionCloseReason.NO_BIDS, LocalDateTime.now(KST));
            eventPublisher.publishAuctionUnsold(auctionId);
            afterCommitRemoveIndex(auctionId); // 트랜잭션 커밋 후에 제거
            log.info("Auction closed as UNSOLD (no bids). auctionId={}", auctionId);
            return;
        }

        // 낙찰 처리
        WinnerSnapshot winner = winnerOpt.get();
        auction.setWinner(winner.memberId(), winner.bidId(), winner.amount());
        auction.markClosed(AuctionCloseReason.SOLD, LocalDateTime.now(KST));

        Instant closedAt = auction.getClosedAt().atZone(KST).toInstant();
        eventPublisher.publishAuctionSold(auctionId, winner.memberId(), winner.bidId(), winner.amount(), closedAt);

        afterCommitCreateEscrow(auctionId, winner.memberId(), winner.amount());
        afterCommitRemoveIndex(auctionId); // 커밋 성공 후에 제거
        log.info("Auction closed as SOLD. auctionId={}, winner={}, amount={}", auctionId, winner.memberId(),
                winner.amount());
    }

    /* DB 트랜잭션이 성공적으로 커밋된 후에 블록체인 에스크로 생성을 시작 */
    private void afterCommitCreateEscrow(Long auctionId, Long winnerMemberId, BigDecimal amount) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("DB transaction for Auction ID {} commited. Starting blockchain escrow creation...", auctionId);
                settlementService.createEscrowForAuction(auctionId, winnerMemberId, amount);
            }
        });
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
}
