package com.bukadong.tcg.api.auction.bootstrap;

import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.auction.util.AuctionRedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 데드라인 ZSET 부트스트랩
 * <P>
 * 가까운 기간(기본 14일) 내 마감하는 OPEN 경매만 Redis ZSET에 적재한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auction.deadline.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class AuctionDeadlineBootstrap {

    private final AuctionRepository auctionRepository;
    private final StringRedisTemplate redis;

    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        Instant now = Instant.now();
        Instant horizon = now.plus(Integer.getInteger("auction.deadline.bootstrap.days", 14), ChronoUnit.DAYS);

        // MySQL 기준: unix_timestamp * 1000 로 epochMillis 생성
        List<Object[]> rows = auctionRepository.findOpenEndAtBefore(horizon);
        for (Object[] row : rows) {
            Long auctionId = (Long) row[0];
            Long endAtMillis = (Long) row[1];
            redis.opsForZSet().add(AuctionRedisKeys.DEADLINES_ZSET, String.valueOf(auctionId),
                    endAtMillis.doubleValue());
        }
    }
}
