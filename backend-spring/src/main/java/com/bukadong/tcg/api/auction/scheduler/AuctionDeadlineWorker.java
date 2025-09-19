package com.bukadong.tcg.api.auction.scheduler;

import com.bukadong.tcg.api.auction.service.AuctionFinalizeService;
import com.bukadong.tcg.api.auction.util.AuctionRedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * 경매 마감 워커
 * <P>
 * Redis ZSET(DEADLINES_ZSET)의 score(now 이하)를 조회하여 DB에서 OPEN→CLOSED 전이를 시도한다. 성공
 * 시 결과/알림 이벤트가 이어서 발생한다. 연장되면 score가 미래로 바뀌므로 재예약이 불필요하다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auction.deadline.worker.enabled", havingValue = "true", matchIfMissing = true)
public class AuctionDeadlineWorker {

    private final StringRedisTemplate redis;
    private final AuctionFinalizeService finalizeService;

    /**
     * 1초마다 마감 도래 경매를 배치 처리한다.
     */
    @Scheduled(fixedDelayString = "${auction.deadline.worker.delay-ms:1000}")
    public void tick() {
        long now = Instant.now().toEpochMilli();
        List<String> dueList = redis.opsForZSet().rangeByScore(AuctionRedisKeys.DEADLINES_ZSET, 0, now).stream()
                .limit(200).toList();

        if (dueList.isEmpty())
            return;

        for (String auctionIdStr : dueList) {
            try {
                long auctionId = Long.parseLong(auctionIdStr);
                boolean closed = finalizeService.finalizeIfDue(auctionId);
                if (closed) {
                    redis.opsForZSet().remove(AuctionRedisKeys.DEADLINES_ZSET, auctionIdStr);
                }
            } catch (Exception e) {
                log.error("Auction finalize failed. auctionId={}", auctionIdStr, e);
            }
        }
    }
}
