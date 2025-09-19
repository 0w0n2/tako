package com.bukadong.tcg.api.bid.service;

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
 * @PARAM amount 입찰가
 * @PARAM eventId 멱등키(=requestId)
 * @RETURN Map(code, currentPriceAfter)
 */
// 변경 이후 코드의 코드블럭만
@Service
@RequiredArgsConstructor
public class BidQueueProducer {

    private static final String AUCTION_KEY_PREFIX = "auction:";
    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<List> bidAtomicScript; // ⬅️ 빈 주입

    /**
     * 입찰 원자 검증/적재
     * <P>
     * 멱등 TTL을 30분으로 상향(재전송/지연 대비).
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @PARAM memberId 입찰자
     * @PARAM amount 입찰가
     * @PARAM eventId 멱등키(=requestId)
     * @RETURN Map(code, currentPriceAfter)
     */
    public Map<String, String> enqueue(Long auctionId, Long memberId, BigDecimal amount, String eventId) {
        String auctionKey = AUCTION_KEY_PREFIX + auctionId;
        String queueKey = AUCTION_KEY_PREFIX + auctionId + ":bidq";
        String idemKey = "idem:" + eventId;

        long now = Instant.now().getEpochSecond();
        String priceStr = amount.toPlainString();

        String payloadOk = String.format(
                "{\"event\":\"BID\",\"intended\":\"ACCEPT\",\"reason\":null,"
                        + "\"auctionId\":%d,\"memberId\":%d,\"amount\":\"%s\",\"eventId\":\"%s\",\"ts\":%d}",
                auctionId, memberId, priceStr, eventId, now);

        String payloadMissing = String.format(
                "{\"event\":\"BID\",\"intended\":\"REJECT\",\"reason\":\"MISSING\","
                        + "\"auctionId\":%d,\"memberId\":%d,\"amount\":\"%s\",\"eventId\":\"%s\",\"ts\":%d}",
                auctionId, memberId, priceStr, eventId, now);

        String payloadNotRunning = String.format(
                "{\"event\":\"BID\",\"intended\":\"REJECT\",\"reason\":\"NOT_RUNNING\","
                        + "\"auctionId\":%d,\"memberId\":%d,\"amount\":\"%s\",\"eventId\":\"%s\",\"ts\":%d}",
                auctionId, memberId, priceStr, eventId, now);

        String payloadLowPrice = String.format(
                "{\"event\":\"BID\",\"intended\":\"REJECT\",\"reason\":\"LOW_PRICE\","
                        + "\"auctionId\":%d,\"memberId\":%d,\"amount\":\"%s\",\"eventId\":\"%s\",\"ts\":%d}",
                auctionId, memberId, priceStr, eventId, now);

        String payloadSelfBid = String.format(
                "{\"event\":\"BID\",\"intended\":\"REJECT\",\"reason\":\"SELF_BID\","
                        + "\"auctionId\":%d,\"memberId\":%d,\"amount\":\"%s\",\"eventId\":\"%s\",\"ts\":%d}",
                auctionId, memberId, priceStr, eventId, now);

        List<String> keys = Arrays.asList(auctionKey, queueKey, idemKey);

        @SuppressWarnings("unchecked")
        List<String> ret = redisTemplate.execute(bidAtomicScript, keys, priceStr, // ARGV[1]
                String.valueOf(now), // ARGV[2]
                "1800", // ARGV[3]
                payloadOk, // ARGV[4]
                payloadMissing, // ARGV[5]
                payloadNotRunning, // ARGV[6]
                payloadLowPrice, // ARGV[7]
                String.valueOf(memberId), // ARGV[8] bidderId (문자열로 전달!)
                payloadSelfBid // ARGV[9]
        );

        String code;
        String curAfter;
        if (ret == null || ret.isEmpty()) {
            code = "ERROR";
            curAfter = "";
        } else {
            code = String.valueOf(ret.get(0));
            curAfter = (ret.size() >= 2) ? String.valueOf(ret.get(1)) : "";
        }

        return Map.of("code", code, "currentPriceAfter", curAfter);
    }
}
