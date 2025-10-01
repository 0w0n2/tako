package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.dto.response.AuctionCancelCheckResponse;
import com.bukadong.tcg.api.auction.dto.response.AuctionCancelResponse;
import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionCloseReason;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.bid.repository.AuctionBidRepository;
import com.bukadong.tcg.api.bid.repository.AuctionLockRepository;
import com.bukadong.tcg.api.bid.service.AuctionCacheService;
import com.bukadong.tcg.api.notification.service.NotificationCommandService;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 경매 취소 서비스
 * <P>
 * 사용자 검증 취소 및 관리자 강제 종료를 처리한다. DB 트랜잭션 완료 후 Redis is_end=1로 동기화한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN AuctionCancelResponse
 */
@Service
@RequiredArgsConstructor
public class AuctionCancelService {
    private final AuctionLockRepository auctionLockRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionBidRepository auctionBidRepository;
    private final AuctionCacheService auctionCacheService; // Redis is_end 갱신 재사용
    private final NotificationCommandService notificationCommandService;
    private static final ZoneOffset UTC = ZoneOffset.UTC;

    /**
     * 사용자 주도 취소
     * <P>
     * 조건: 본인 경매, is_end=0, 종료시간 이전, 입찰내역 없음.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @PARAM memberId 요청자(판매자) ID
     * @RETURN 취소 결과
     */
    @Transactional
    public AuctionCancelResponse cancelByOwner(Long auctionId, Long memberId) {
        Auction auction = auctionLockRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        // 본인 경매인지 확인
        if (auction.getMember() == null || !auction.getMember().getId().equals(memberId)) {
            throw new BaseException(BaseResponseStatus.AUCTION_UNAUTHORIZED);
        }
        // 이미 종료되었는지
        if (auction.isEnd()) {
            throw new BaseException(BaseResponseStatus.AUCTION_ALREADY_ENDED); // 이미 종료/취소
        }
        // 종료시간 이전인지
        if (!LocalDateTime.now(UTC).isBefore(auction.getEndDatetime())) {
            throw new BaseException(BaseResponseStatus.AUCTION_NOT_RUNNING); // 기간 외
        }
        // 입찰 없음 확인
        if (auctionBidRepository.existsByAuction_Id(auctionId)) {
            throw new BaseException(BaseResponseStatus.AUCTION_EXISTING_BID); // 입찰 존재로 취소 불가
        }

        LocalDateTime now = LocalDateTime.now(UTC);
        int updated = auctionRepository.closeManually(auctionId, AuctionCloseReason.SELLER_CANCEL, now);
        if (updated <= 0) {
            // 동시성으로 상태가 바뀐 경우
            throw new BaseException(BaseResponseStatus.AUCTION_CONFLICT);
        }

        // 트랜잭션 커밋 이후 Redis is_end=1로 동기화
        afterCommitMarkRedisEnded(auctionId);

        // 판매자 취소 알림
        notificationCommandService.notifySellerCanceled(memberId, auctionId);

        return AuctionCancelResponse.builder().auctionId(auctionId).cancelledBy("USER").cancelledAt(now).build();
    }

    /**
     * 관리자 강제 종료
     * <P>
     * 바로 종료 처리. 조건 검증 없이 is_end=1.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @RETURN 취소 결과
     */
    @Transactional
    public AuctionCancelResponse cancelByAdmin(Long auctionId) {
        // 존재성만 보장 (락으로 경합 방지)
        auctionLockRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        LocalDateTime now = LocalDateTime.now(UTC);
        int updated = auctionRepository.closeManually(auctionId, AuctionCloseReason.ADMIN_CANCEL, now);
        if (updated <= 0) {
            // 이미 종료 등
            throw new BaseException(BaseResponseStatus.AUCTION_CONFLICT);
        }
        // 트랜잭션 커밋 이후 Redis is_end=1로 동기화
        afterCommitMarkRedisEnded(auctionId);

        // 관리자 강제 종료 알림 (판매자)
        Long sellerId = auctionRepository.findById(auctionId)
                .map(a -> a.getMember() != null ? a.getMember().getId() : null).orElse(null);

        notificationCommandService.notifyAdminCanceled(sellerId, auctionId);
        return AuctionCancelResponse.builder().auctionId(auctionId).cancelledBy("ADMIN").cancelledAt(now).build();
    }

    /** 커밋 후 Redis is_end=1 동기화 */
    private void afterCommitMarkRedisEnded(Long auctionId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                auctionCacheService.markEnded(auctionId);
            }
        });
    }

    // 변경 이후 코드의 코드블럭만

    /**
     * 사용자 취소 가능 여부 점검
     * <P>
     * 로직: 본인 경매인지 → DB 종료 플래그 → now < end → 입찰 존재 순으로 검사. 실패 시에도 200 OK로
     * allowed=false와 reason만 내려 UI가 분기하기 쉽도록 한다.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @PARAM memberId 요청자(판매자) ID
     * @RETURN AuctionCancelCheckResponse
     */

    @Transactional(readOnly = true)
    public AuctionCancelCheckResponse checkCancelableByOwner(Long auctionId, Long memberId) {
        var optional = auctionRepository.findById(auctionId);
        if (optional.isEmpty()) {
            throw new BaseException(BaseResponseStatus.NOT_FOUND);
        }
        var a = optional.get();
        var now = LocalDateTime.now(UTC);

        boolean owner = a.getMember() != null && a.getMember().getId().equals(memberId);
        if (!owner) {
            return AuctionCancelCheckResponse.of(auctionId, "NOT_OWNER");
        }

        if (a.isEnd()) {
            return AuctionCancelCheckResponse.of(auctionId, "ALREADY_ENDED");
        }

        boolean timeOver = !now.isBefore(a.getEndDatetime()); // now >= end
        if (timeOver) {
            return AuctionCancelCheckResponse.of(auctionId, "TIME_OVER");
        }

        boolean hasBid = auctionBidRepository.existsByAuction_Id(auctionId);
        if (hasBid) {
            return AuctionCancelCheckResponse.of(auctionId, "HAS_BID");
        }

        // 모두 통과
        return AuctionCancelCheckResponse.of(auctionId, "OK");
    }
}
