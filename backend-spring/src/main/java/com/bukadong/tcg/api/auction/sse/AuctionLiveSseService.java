package com.bukadong.tcg.api.auction.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * 경매 라이브 SSE 서비스 - 목록: 선택 구독(ids) → price만 - 상세: 단일 경매 구독 → price, end_ts, end,
 * bid(닉네임/시간)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionLiveSseService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String FIELD_AUCTION_ID = "auctionId";
    private static final String FIELD_CURRENT_PRICE = "currentPrice";
    private static final String FIELD_END_TS = "endTs";
    private static final String FIELD_IS_END = "isEnd";

    // 목록 구독자: price만 전파
    private final Map<Long, Set<SseEmitter>> perAuctionListEmitters = new ConcurrentHashMap<>();
    // 상세 구독자: price/end_ts/end/bid 전파
    private final Map<Long, Set<SseEmitter>> perAuctionDetailEmitters = new ConcurrentHashMap<>();
    // 선택 구독 역인덱스(해제용)
    private final Map<SseEmitter, Set<Long>> emitterToAuctionIds = new ConcurrentHashMap<>();

    private static final long DEFAULT_TIMEOUT_MS = 30 * 60 * 1000L; // 30분

    /** 상세: 단일 경매 구독 */
    public SseEmitter subscribeAuction(long auctionId, Map<String, String> snapshot) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        attachLifecycle(emitter, auctionId);
        perAuctionDetailEmitters.computeIfAbsent(auctionId, k -> new CopyOnWriteArraySet<>()).add(emitter);
        // 스냅샷 1회 전송
        sendEvent(emitter, "snapshot",
                Map.of(FIELD_AUCTION_ID, auctionId, FIELD_CURRENT_PRICE, snapshot.getOrDefault("current_price", "0"),
                        FIELD_END_TS, snapshot.getOrDefault("end_ts", "0"), FIELD_IS_END,
                        snapshot.getOrDefault("is_end", "0")));
        return emitter;
    }

    /** 목록: 선택 경매 다건 구독 */
    public SseEmitter subscribeSelected(Set<Long> auctionIds, List<Map<String, String>> snapshots) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        attachLifecycle(emitter, null);
        emitterToAuctionIds.put(emitter, new CopyOnWriteArraySet<>(auctionIds));
        for (Long id : auctionIds) {
            perAuctionListEmitters.computeIfAbsent(id, k -> new CopyOnWriteArraySet<>()).add(emitter);
        }
        sendEvent(emitter, "snapshot_many", snapshots);
        return emitter;
    }

    /** 가격 변경 브로드캐스트 (목록+상세) */
    public void publishPriceUpdate(long auctionId, String currentPrice, Long endTsEpochSec) {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put(FIELD_AUCTION_ID, auctionId);
        payload.put(FIELD_CURRENT_PRICE, currentPrice);
        if (endTsEpochSec != null)
            payload.put(FIELD_END_TS, endTsEpochSec);
        // 목록 전파
        Set<SseEmitter> lset = perAuctionListEmitters.get(auctionId);
        if (lset != null)
            for (SseEmitter e : lset)
                sendEvent(e, "price", payload);
        // 상세 전파
        Set<SseEmitter> dset = perAuctionDetailEmitters.get(auctionId);
        if (dset != null)
            for (SseEmitter e : dset)
                sendEvent(e, "price", payload);
    }

    /** 마감시간 변경 (상세 전용) */
    public void publishEndTsUpdate(long auctionId, long endTsEpochSec) {
        Map<String, Object> payload = Map.of(FIELD_AUCTION_ID, auctionId, FIELD_END_TS, endTsEpochSec);
        Set<SseEmitter> dset = perAuctionDetailEmitters.get(auctionId);
        if (dset != null)
            for (SseEmitter e : dset)
                sendEvent(e, "end_ts", payload);
    }

    /** 종료 알림 (상세 전용) */
    public void publishEnded(long auctionId) {
        Map<String, Object> payload = Map.of(FIELD_AUCTION_ID, auctionId, FIELD_IS_END, 1);
        Set<SseEmitter> dset = perAuctionDetailEmitters.get(auctionId);
        if (dset != null)
            for (SseEmitter e : dset)
                sendEvent(e, "end", payload);
    }

    /** 입찰 수락 (상세 전용) - 닉네임/시간 문자열 */
    public void publishBidAccepted(long auctionId, String nickname, String amount, String timeIso) {
        Map<String, Object> payload = Map.of(FIELD_AUCTION_ID, auctionId, "nickname", nickname, "amount", amount,
                "time", timeIso);
        Set<SseEmitter> dset = perAuctionDetailEmitters.get(auctionId);
        if (dset != null)
            for (SseEmitter e : dset)
                sendEvent(e, "bid", payload);
    }

    /** 즉시구매 발생 (상세 전용) - 닉네임/시간/금액 */
    public void publishBuyNow(long auctionId, String nickname, String amount, String timeIso) {
        Map<String, Object> payload = Map.of(FIELD_AUCTION_ID, auctionId, "nickname", nickname, "amount", amount,
                "time", timeIso);
        Set<SseEmitter> dset = perAuctionDetailEmitters.get(auctionId);
        if (dset != null)
            for (SseEmitter e : dset)
                sendEvent(e, "buy_now", payload);
    }

    /** 주기적 하트비트 */
    @Scheduled(fixedRateString = "${auction.sse.heartbeat-ms:15000}")
    public void heartbeat() {
        for (Set<SseEmitter> set : perAuctionListEmitters.values()) {
            for (SseEmitter e : set)
                sendEvent(e, "heartbeat", Map.of("ts", Instant.now().toEpochMilli()));
        }
        for (Set<SseEmitter> set : perAuctionDetailEmitters.values()) {
            for (SseEmitter e : set)
                sendEvent(e, "heartbeat", Map.of("ts", Instant.now().toEpochMilli()));
        }
    }

    private void attachLifecycle(SseEmitter emitter, Long auctionIdOrNull) {
        emitter.onCompletion(() -> removeEmitter(emitter, auctionIdOrNull));
        emitter.onTimeout(() -> removeEmitter(emitter, auctionIdOrNull));
        emitter.onError(ex -> removeEmitter(emitter, auctionIdOrNull));
    }

    private void removeEmitter(SseEmitter emitter, Long auctionIdOrNull) {
        if (auctionIdOrNull != null) {
            Set<SseEmitter> lset = perAuctionListEmitters.get(auctionIdOrNull);
            if (lset != null)
                lset.remove(emitter);
            Set<SseEmitter> dset = perAuctionDetailEmitters.get(auctionIdOrNull);
            if (dset != null)
                dset.remove(emitter);
        } else {
            Set<Long> ids = emitterToAuctionIds.remove(emitter);
            if (ids != null) {
                for (Long id : ids) {
                    Set<SseEmitter> lset = perAuctionListEmitters.get(id);
                    if (lset != null)
                        lset.remove(emitter);
                }
            } else {
                for (Set<SseEmitter> set : perAuctionListEmitters.values())
                    set.remove(emitter);
                for (Set<SseEmitter> set : perAuctionDetailEmitters.values())
                    set.remove(emitter);
            }
        }
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) {
        try {
            SseEmitter.SseEventBuilder evt = SseEmitter.event().name(name).data(data, MediaType.APPLICATION_JSON);
            emitter.send(evt);
        } catch (IOException | IllegalStateException e) {
            removeEmitter(emitter, null);
        }
    }

    /** 특정 경매의 Redis 스냅샷을 조회 */
    public Map<Object, Object> readSnapshot(long auctionId) {
        String key = "auction:" + auctionId;
        return redisTemplate.opsForHash().entries(key);
    }
}
