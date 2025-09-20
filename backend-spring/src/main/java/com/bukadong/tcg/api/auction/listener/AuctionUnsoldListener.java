package com.bukadong.tcg.api.auction.listener;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionResult;
import com.bukadong.tcg.api.auction.event.AuctionSoldEvent;
import com.bukadong.tcg.api.auction.event.AuctionUnsoldEvent;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.auction.repository.AuctionResultRepository;
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
public class AuctionUnsoldListener {

    private final AuctionRepository auctionRepository;
    private final NotificationCommandService notificationService; // 알림 도메인 서비스

    /**
     * 결과 저장 및 알림 발송(멱등)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUnsold(AuctionUnsoldEvent e) {
        try {
            auctionRepository.findById(e.auctionId()).ifPresent(a -> {
                if (a.getMember() != null) {
                    Long sellerId = a.getMember().getId();
                    // 유찰용 판매자 알림
                    notificationService.notifyAuctionSellerClosedUnsold(sellerId, e.auctionId());
                }
            });
        } catch (Exception ex) {
            log.error("Notification failed for UNSOLD auctionId={}", e.auctionId(), ex);
        }
    }

}
