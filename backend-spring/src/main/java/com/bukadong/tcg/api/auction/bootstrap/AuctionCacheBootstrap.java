package com.bukadong.tcg.api.auction.bootstrap;

import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.bid.service.AuctionCacheService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 애플리케이션 기동 시 미종료 경매의 Redis 캐시를 워밍한다. - auction:{id} 해시에
 * current_price/bid_unit/start_ts/end_ts/owner_id/is_end 채움
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auction.cache.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class AuctionCacheBootstrap {

    private static final Logger log = LoggerFactory.getLogger(AuctionCacheBootstrap.class);

    private final AuctionRepository auctionRepository;
    private final AuctionCacheService auctionCacheService;

    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        List<Long> openIds = auctionRepository.findAllOpenIds();
        int cnt = 0;
        for (Long id : openIds) {
            try {
                auctionCacheService.ensureLoaded(id);
                cnt++;
            } catch (Exception e) {
                log.warn("Failed to warm cache for auctionId={}: {}", id, e.toString());
            }
        }
        log.info("Auction cache bootstrap done. warmed={} totalOpen={}", cnt, openIds.size());
    }
}
