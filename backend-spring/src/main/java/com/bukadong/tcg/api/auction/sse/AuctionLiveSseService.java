package com.bukadong.tcg.api.auction.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    // 기본 타임아웃 (환경설정으로 조정 가능)
    @Value("${auction.sse.timeout-ms:600000}") // 기본 10분
    private long defaultTimeoutMs;

    @Value("${auction.sse.heartbeat-ms:15000}")
    private long heartbeatMs; // 주입 받아 모니터링용 (스케줄 주기는 기존 @Scheduled 유지)

    // 끊긴 emitter 감지용 (옵션적) 최근 실패 카운트
    private final Map<SseEmitter, Integer> failureCounts = new ConcurrentHashMap<>();

    /** 상세: 단일 경매 구독 */
    public SseEmitter subscribeAuction(long auctionId, Map<String, String> snapshot) {
        SseEmitter emitter = new SseEmitter(defaultTimeoutMs);
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
        SseEmitter emitter = new SseEmitter(defaultTimeoutMs);
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
        if (isDeadBid(auctionId, amount)) {
            log.debug("[SSE] skip dead bid auctionId={} amount={} time={}", auctionId, amount, timeIso);
            return;
        }

        Map<String, Object> payload = Map.of(FIELD_AUCTION_ID, auctionId, "nickname", nickname, "amount", amount,
                "time", timeIso);
        Set<SseEmitter> dset = perAuctionDetailEmitters.get(auctionId);
        if (dset != null)
            for (SseEmitter e : dset)
                sendEvent(e, "bid", payload);
    }

    /** Dead bid 여부 판단: 종료되었거나 current_price >= incoming */
    private boolean isDeadBid(long auctionId, String amount) {
        try {
            String key = "auction:" + auctionId;
            Map<Object, Object> snap = redisTemplate.opsForHash().entries(key);
            if (snap == null || snap.isEmpty())
                return false; // 캐시 없으면 판단 불가 → 전파
            Object endFlag = snap.get("is_end");
            if ("1".equals(endFlag))
                return true; // 종료된 경매
            Object curObj = snap.get("current_price");
            if (curObj == null)
                return false; // 현재가 없으면 비교 불가 → 전파
            return isPriceStale(curObj.toString(), amount);
        } catch (Exception e) {
            log.warn("[SSE] isDeadBid check error auctionId={} msg={}", auctionId, e.getMessage());
            return false; // 에러 시 전파
        }
    }

    private boolean isPriceStale(String currentPrice, String incomingPrice) {
        try {
            java.math.BigDecimal incoming = new java.math.BigDecimal(incomingPrice);
            java.math.BigDecimal current = new java.math.BigDecimal(currentPrice);
            // 기존 '<= 0' (같은 금액도 dead 처리) → '< 0' 으로 변경하여 동일 금액은 전파
            return incoming.compareTo(current) < 0;
        } catch (Exception ignore) {
            return false; // 파싱 실패 → 전파 허용
        }
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
        cleanupStale();
    }

    private void attachLifecycle(SseEmitter emitter, Long auctionIdOrNull) {
        emitter.onCompletion(() -> removeEmitter(emitter, auctionIdOrNull));
        emitter.onTimeout(() -> removeEmitter(emitter, auctionIdOrNull));
        emitter.onError(ex -> removeEmitter(emitter, auctionIdOrNull));
    }

    private void removeEmitter(SseEmitter emitter, Long auctionIdOrNull) {
        // 1. 상세 또는 목록 한 경매에만 직접 매핑된 경우
        if (auctionIdOrNull != null) {
            removeFromSetMap(perAuctionListEmitters, auctionIdOrNull, emitter);
            removeFromSetMap(perAuctionDetailEmitters, auctionIdOrNull, emitter);
        }

        // 2. 선택 구독(emitterToAuctionIds) 기반 제거
        Set<Long> selected = emitterToAuctionIds.remove(emitter);
        if (selected != null) {
            for (Long id : selected) {
                removeFromSetMap(perAuctionListEmitters, id, emitter);
            }
        }

        // 3. 어떠한 매핑에도 없지만 여전히 남아 있을 수 있는 경우(안전망)
        // 큰 비용 없도록 조건식 간단 유지
        if (selected == null && auctionIdOrNull == null) {
            sweepEmitterFromAll(perAuctionListEmitters, emitter);
            sweepEmitterFromAll(perAuctionDetailEmitters, emitter);
        }

        failureCounts.remove(emitter); // 실패 카운트도 정리
    }

    private void removeFromSetMap(Map<Long, Set<SseEmitter>> map, Long key, SseEmitter emitter) {
        Set<SseEmitter> set = map.get(key);
        if (set != null)
            set.remove(emitter);
    }

    private void sweepEmitterFromAll(Map<Long, Set<SseEmitter>> map, SseEmitter emitter) {
        for (Set<SseEmitter> set : map.values()) {
            set.remove(emitter);
        }
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) {
        try {
            SseEmitter.SseEventBuilder evt = SseEmitter.event().name(name).data(data, MediaType.APPLICATION_JSON);
            emitter.send(evt);
            failureCounts.remove(emitter); // 성공 시 실패 카운트 초기화
        } catch (IOException | IllegalStateException e) {
            String msg = e.getMessage();
            boolean clientAbort = msg != null && (msg.contains("Broken pipe") || msg.contains("Connection reset")
                    || msg.contains("호스트 시스템") || msg.contains("forcibly closed")
                    || msg.contains("An existing connection was forcibly closed"));
            if (clientAbort) {
                log.debug("[SSE] client disconnected ({}): {}", name, msg);
            } else {
                log.warn("[SSE] send failed event={} msg={}", name, msg);
            }
            failureCounts.merge(emitter, 1, Integer::sum);
            removeEmitter(emitter, null);
        }
    }

    /** 최근 실패 다수 발생한 emitter 정리 (이중 안전망) */
    private void cleanupStale() {
        if (failureCounts.isEmpty())
            return;
        // 실패 카운트가 남아 있으나 맵에서 제거된 emitter 정리
        failureCounts.keySet()
                .removeIf(e -> !emitterToAuctionIds.containsKey(e)
                        && perAuctionListEmitters.values().stream().noneMatch(set -> set.contains(e))
                        && perAuctionDetailEmitters.values().stream().noneMatch(set -> set.contains(e)));
    }

    /** 특정 경매의 Redis 스냅샷을 조회 */
    public Map<Object, Object> readSnapshot(long auctionId) {
        String key = "auction:" + auctionId;
        return redisTemplate.opsForHash().entries(key);
    }
}
