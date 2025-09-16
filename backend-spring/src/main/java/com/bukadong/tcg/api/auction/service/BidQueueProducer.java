package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.util.AuctionBidLuaScripts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 큐 기반 입찰 프로듀서
 * <P>
 * Lua로 검증/현재가 갱신/이벤트 적재/멱등키를 원자 수행.
 * </P>
 * 
 * @PARAM auctionId 경매 ID
 * @PARAM memberId 입찰자 ID
 * @PARAM bidPrice 입찰가
 * @PARAM eventId 멱등키(=requestId)
 * @RETURN Map(code, currentPriceAfter)
 */
@Service
@RequiredArgsConstructor
public class BidQueueProducer {

    private final RedisTemplate<String, String> redisTemplate;

    public Map<String, String> enqueue(Long auctionId, Long memberId, BigDecimal bidPrice, String eventId) {
        String auctionKey = "auction:" + auctionId;
        String queueKey = "auction:" + auctionId + ":bidq";
        String idemKey = "idem:" + eventId;

        long now = Instant.now().getEpochSecond();

        String payloadOk = String.format(
                "{\"event\":\"BID\",\"intended\":\"ACCEPT\",\"reason\":null,"
                        + "\"auctionId\":%d,\"memberId\":%d,\"bidPrice\":\"%s\",\"eventId\":\"%s\",\"ts\":%d}",
                auctionId, memberId, bidPrice.toPlainString(), eventId, now);

        String payloadReject = String.format(
                "{\"event\":\"BID\",\"intended\":\"REJECT\",\"reason\":\"PRECHECK\","
                        + "\"auctionId\":%d,\"memberId\":%d,\"bidPrice\":\"%s\",\"eventId\":\"%s\",\"ts\":%d}",
                auctionId, memberId, bidPrice.toPlainString(), eventId, now);

        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(AuctionBidLuaScripts.BID_ATOMIC);
        script.setResultType(List.class);

        List<String> keys = Arrays.asList(auctionKey, queueKey, idemKey);
        @SuppressWarnings("unchecked")
        List<String> ret = (List<String>) redisTemplate.execute(script, keys, bidPrice.toPlainString(), // ARGV[1]
                String.valueOf(now), // ARGV[2]
                "60", // ARGV[3] idem TTL
                payloadOk, // ARGV[4]
                payloadReject // ARGV[5]
        );

        String code = ret != null && ret.size() > 0 ? String.valueOf(ret.get(0)) : "ERROR";
        String curAfter = ret != null && ret.size() > 1 ? String.valueOf(ret.get(1)) : "";

        return Map.of("code", code, "currentPriceAfter", curAfter);
    }
}
