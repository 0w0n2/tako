package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.event.AuctionClosedEvent;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * 경매 종료 확정 서비스
 * <P>
 * DB에서 status=OPEN && end_at<=now 조건으로 CLOSED 전이를 원자적으로 시도한다. 영향 행이 1이면 유일 종료자로
 * 확정되며 낙찰 스냅샷을 조회해 도메인 이벤트를 발행한다.
 * </P>
 * 
 * @PARAM auctionId 경매 ID
 * @RETURN true(이번 호출이 종료 처리자) / false(연장/중복 처리 등)
 */
@Service
@RequiredArgsConstructor
public class AuctionFinalizeService {

    private final AuctionRepository auctionRepository;
    private final AuctionWinnerQuery winnerQuery;
    private final ApplicationEventPublisher publisher;
    private final Clock clock = Clock.systemDefaultZone();

    /**
     * 종료 조건 만족 시 CLOSED 전이 + 이벤트 발행
     */
    @Transactional
    public boolean finalizeIfDue(long auctionId) {
        int updated = auctionRepository.closeIfDue(auctionId);
        if (updated == 1) {
            var w = winnerQuery.getWinnerSnapshot(auctionId);
            publisher.publishEvent(
                    new AuctionClosedEvent(auctionId, w.memberId(), w.amount(), w.bidId(), clock.instant()));
            return true;
        }
        return false;
    }
}
