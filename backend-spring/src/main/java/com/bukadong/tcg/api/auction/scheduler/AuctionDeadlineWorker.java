package com.bukadong.tcg.api.auction.scheduler;

import java.util.List;
import java.util.concurrent.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.bukadong.tcg.api.auction.service.AuctionFinalizeService;
import com.bukadong.tcg.api.auction.service.AuctionQueryService;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class AuctionDeadlineWorker {

    private static final Logger log = LoggerFactory.getLogger(AuctionDeadlineWorker.class);

    private final AuctionQueryService auctionQueryService;
    private final AuctionFinalizeService auctionFinalizeService;

    @Value("${auction.finalize.batch-size:100}")
    private int batchSize;

    @Value("${auction.finalize.max-tick-ms:3000}")
    private long maxTickMs;

    @Value("${auction.finalize.parallelism:1}")
    private int parallelism;

    // 병렬 처리가 필요하면 간단한 풀 사용(1이면 순차 처리)
    private ExecutorService pool;

    private ExecutorService pool() {
        if (parallelism <= 1)
            return null;
        if (pool == null) {
            pool = new ThreadPoolExecutor(parallelism, parallelism, 60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(parallelism * 4), r -> {
                        Thread t = new Thread(r, "auction-finalize");
                        t.setDaemon(true);
                        return t;
                    }, new ThreadPoolExecutor.CallerRunsPolicy());
        }
        return pool;
    }

    @Scheduled(fixedDelayString = "${auction.finalize.tick-ms:1000}")
    public void tick() {
        final long startMs = System.currentTimeMillis();
        int processed = 0;
        int loops = 0;

        while (true) {
            // 1) 한 배치 조회
            List<Long> dueIds = auctionQueryService.findDueAuctionIds(batchSize);
            if (dueIds.isEmpty())
                break;

            // 2) 처리 (순차 또는 병렬)
            if (parallelism <= 1) {
                for (Long id : dueIds) {
                    try {
                        auctionFinalizeService.finalizeIfDue(id);
                        processed++;
                    } catch (BaseException e) {
                        log.warn("Finalize skipped by domain rule. auctionId={}, err={}", id, e.getStatus());
                    } catch (Exception e) {
                        log.error("Finalize failed by unexpected error. auctionId={}", id, e);
                    }
                }
            } else {
                ExecutorService exec = pool();
                CountDownLatch latch = new CountDownLatch(dueIds.size());
                for (Long id : dueIds) {
                    exec.execute(() -> {
                        try {
                            auctionFinalizeService.finalizeIfDue(id);
                        } catch (BaseException e) {
                            log.warn("Finalize skipped by domain rule. auctionId={}, err={}", id, e.getStatus());
                        } catch (Exception e) {
                            log.error("Finalize failed by unexpected error. auctionId={}", id, e);
                        } finally {
                            latch.countDown();
                        }
                    });
                }
                try {
                    latch.await(Math.max(1000, maxTickMs), TimeUnit.MILLISECONDS);
                } catch (InterruptedException ignored) {
                }
                processed += dueIds.size();
            }

            loops++;

            // 3) 더 가져올 게 없으면 종료
            if (dueIds.size() < batchSize)
                break;

            // 4) 시간 예산 소진 시 다음 tick으로 넘김(스케줄러 독점 방지)
            long elapsed = System.currentTimeMillis() - startMs;
            if (elapsed >= maxTickMs) {
                log.info("Finalize tick time budget reached. processed={}, loops={}, elapsedMs={}", processed, loops,
                        elapsed);
                break;
            }
        }

        long elapsed = System.currentTimeMillis() - startMs;
        if (processed > 0) {
            log.info("Finalize tick done. processed={}, loops={}, elapsedMs={}", processed, loops, elapsed);
        } else {
            log.debug("Finalize tick done. processed=0, elapsedMs={}", elapsed);
        }
    }
}
