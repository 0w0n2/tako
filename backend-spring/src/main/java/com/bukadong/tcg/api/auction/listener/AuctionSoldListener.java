package com.bukadong.tcg.api.auction.listener;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionResult;
import com.bukadong.tcg.api.auction.event.AuctionSoldEvent;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.auction.repository.AuctionResultRepository;
import com.bukadong.tcg.api.auction.service.AuctionSettlementService;
import com.bukadong.tcg.api.auction.util.AuctionDeadlineIndex;
import com.bukadong.tcg.api.bid.repository.AuctionBidRepository;
import com.bukadong.tcg.api.notification.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 경매 종료 리스너
 * <P>
 * 경매 결과를 멱등 저장하고, 구매자/판매자 알림을 발송한다. AuctionResult는 Auction/AuctionBid 엔티티 연관관계를
 * 그대로 저장한다.
 * </P>
 * 
 * @PARAM AuctionClosedEvent 종료 이벤트
 * @RETURN 없음
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionSoldListener {

    private final AuctionResultRepository resultRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionBidRepository bidRepository;
    private final NotificationCommandService notificationService; // 알림 도메인 서비스
    private final AuctionSettlementService auctionSettlementService;

    /**
     * 결과 저장 및 알림 발송
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onClosed(AuctionSoldEvent e) {

        // 이미 결과가 있으면 스킵
        boolean exists = resultRepository.existsByAuction_Id(e.auctionId());
        if (!exists) {
            Auction auction = auctionRepository.findById(e.auctionId())
                    .orElseThrow(() -> new IllegalStateException("Auction not found: " + e.auctionId()));
            var bid = bidRepository.findById(e.bidId())
                    .orElseThrow(() -> new IllegalStateException("Bid not found: " + e.bidId()));

            // 종료 직후에는 미정산
            AuctionResult result = AuctionResult.builder().auction(auction).auctionBid(bid).settledFlag(false)
                    .settleTxHash(null).build();
            resultRepository.save(result);
        }

        // 블록체인 에스크로 생성 시작
        auctionSettlementService.createEscrowForAuction(
                e.auctionId(),
                e.amount(),
                e.seller(),
                e.buyer(),
                e.physicalCard()
        );

        // 알림 발송
        try {
            // 구매자 알림
            notificationService.notifyAuctionWon(e.buyer().getId(), e.auctionId(), e.amount(), e.closedAt());

            // 판매자 알림
            auctionRepository.findById(e.auctionId()).ifPresent(a -> {
                if (a.getMember() != null) {
                    Long sellerId = a.getMember().getId();
                    notificationService.notifyAuctionSellerClosed(sellerId, e.auctionId(), e.amount(), e.closedAt());
                }
            });
        } catch (Exception ex) {
            log.error("Notification failed for auctionId={}", e.auctionId(), ex);
        }
    }

}
