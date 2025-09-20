package com.bukadong.tcg.api.auction.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuctionDeadlineIndex {
    private final StringRedisTemplate redis;
    public static final String DEADLINES_ZSET = "AUCTION:DEADLINES";

    public void upsert(long auctionId, long epochMillis) {
        redis.opsForZSet().add(DEADLINES_ZSET, String.valueOf(auctionId), epochMillis);
    }

    public void remove(long auctionId) {
        redis.opsForZSet().remove(DEADLINES_ZSET, String.valueOf(auctionId));
    }
}
