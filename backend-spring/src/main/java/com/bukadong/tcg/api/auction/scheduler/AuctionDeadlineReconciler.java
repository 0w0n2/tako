package com.bukadong.tcg.api.auction.scheduler;

import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.auction.util.AuctionDeadlineIndex;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auction.deadline.reconcile.enabled", havingValue = "true", matchIfMissing = true)
public class AuctionDeadlineReconciler {

    private static final Logger log = LoggerFactory.getLogger(AuctionDeadlineReconciler.class);

    private final AuctionRepository auctionRepository;
    private final AuctionDeadlineIndex deadlineIndex;

    @Value("${auction.deadline.bootstrap.days:14}")
    private int horizonDays;

    // 기본 5분마다 롤링 스캔
    @Scheduled(cron = "${auction.deadline.reconcile.cron:0 */5 * * * *}")
    public void reconcile() {
        Instant now = Instant.now();
        Instant horizon = now.plus(horizonDays, ChronoUnit.DAYS);

        List<Object[]> rows = auctionRepository.findOpenEndAtBefore(horizon);
        for (Object[] row : rows) {
            Long auctionId = (Long) row[0];
            Long endAtMillis = (Long) row[1];
            deadlineIndex.upsert(auctionId, endAtMillis); // 중복 안전
        }
        log.debug("Reconciled deadlines up to {}", horizon);
    }
}
