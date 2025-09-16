package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.Map;

/**
 * 경매 캐시 서비스
 * <P>
 * DB → Redis 해시 초기화/보정. Lua 스크립트가 참조하는 필드를 채운다.
 * </P>
 * 
 * @PARAM auctionId 경매 ID
 * @RETURN 없음
 */
@Service
@RequiredArgsConstructor
public class AuctionCacheService {

    private final AuctionRepository auctionRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public void ensureLoaded(Long auctionId) {
        Auction a = auctionRepository.findById(auctionId).orElse(null);
        if (a == null)
            return;

        String key = "auction:" + auctionId;
        HashOperations<String, String, String> h = redisTemplate.opsForHash();

        if (Boolean.FALSE.equals(redisTemplate.hasKey(key)) || h.entries(key).isEmpty()
                || h.get(key, "current_price") == null) {
            h.putAll(key, Map.of("is_end", a.isEnd() ? "1" : "0", "start_ts",
                    String.valueOf(a.getStartDatetime().toEpochSecond(ZoneOffset.UTC)), "end_ts",
                    String.valueOf(a.getEndDatetime().toEpochSecond(ZoneOffset.UTC)), "current_price",
                    a.getCurrentPrice().toPlainString(), "bid_unit", a.getBidUnit().toBigDecimal().toPlainString()));
        }
    }

    public void overwritePrice(Long auctionId, String currentPriceStr) {
        String key = "auction:" + auctionId;
        String cur = (String) redisTemplate.opsForHash().get(key, "current_price");
        try {
            if (cur == null || new java.math.BigDecimal(currentPriceStr).compareTo(new java.math.BigDecimal(cur)) > 0) {
                redisTemplate.opsForHash().put(key, "current_price", currentPriceStr);
            }
            // 더 낮으면 무시
        } catch (Exception ignore) {
            // 파싱 실패 등은 안전하게 무시하거나 로그
            redisTemplate.opsForHash().put(key, "current_price", currentPriceStr);
        }
    }

}
