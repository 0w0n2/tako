package com.bukadong.tcg.api.bid.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 입찰 이벤트 컨슈머
 * <P>
 * Redis 큐 → DB 반영(행락)으로 최종 일관성 보장. 기존 구조를 유지하면서 폴링 드레인/재시도/사망 큐를 추가해 안정성을 높였다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BidEventConsumer {

    private final RedisTemplate<String, String> redisTemplate;
    private final BidEventApplyService bidEventApplyService;
    private final AuctionCacheService auctionCacheService;
    private final ObjectMapper om;
    private static final String RETRY_KEY_PREFIX = ":retry";

    /**
     * 간단 폴링
     * <P>
     * - KEYS 대신 SCAN으로 큐 키를 탐색(대량 키 환경에서 안전)<br>
     * - 각 큐에 대해 retry 큐 우선 소진 후, 메인 큐를 비워질 때까지 드레인 처리
     * </P>
     */
    @Scheduled(fixedDelay = 200)
    public void poll() {
        try {
            for (String q : scan("auction:*:bidq", 200)) {
                // retry 큐 먼저 소진
                drain(q + RETRY_KEY_PREFIX, 50);

                // 메인 큐 드레인 (비어질 때까지 처리)
                while (true) {
                    String json = redisTemplate.opsForList().leftPop(q);
                    if (json == null)
                        break;
                    handleOne(q, json);
                }
            }
        } catch (Exception e) {
            log.error("Bid consumer loop error: {}", e.toString());
        }
    }

    /**
     * 메시지 1건 처리 및 재시도/사망 큐 라우팅
     * <P>
     * 일시 실패는 :retry, 영구 실패는 :dead 로 이동.
     * </P>
     * 
     * @PARAM queue 원본 큐 키
     * @PARAM json 이벤트 페이로드
     * @RETURN 없음
     */
    private void handleOne(String queue, String json) {
        try {
            bidEventApplyService.applyEvent(json);
        } catch (RetryableException re) {
            redisTemplate.opsForList().rightPush(queue + RETRY_KEY_PREFIX, json);
            log.warn("Retryable event pushed: {}", re.getMessage());
        } catch (Exception fatal) {
            redisTemplate.opsForList().rightPush(queue + ":dead", json);
            log.error("Dead-lettered event: {}", fatal.toString());
            // 보상: DB 반영 실패로 인한 Redis 가격과 DB 불일치 시 정합성 복구 시도
            try {
                JsonNode n = om.readTree(json);
                if (n != null && n.hasNonNull("auctionId")) {
                    long aid = n.get("auctionId").asLong();
                    auctionCacheService.syncExactCurrentPrice(aid);
                }
            } catch (Exception ignore) {
                // 보수적으로 무시
            }
        }
    }

    /**
     * 재시도 큐 드레인
     * <P>
     * limit 개수만큼 우선 처리.
     * </P>
     * 
     * @PARAM queue 재시도 큐 키
     * @PARAM limit 최대 처리 개수
     * @RETURN 없음
     */
    private void drain(String queue, int limit) {
        for (int i = 0; i < limit; i++) {
            String json = redisTemplate.opsForList().leftPop(queue);
            if (json == null)
                break;
            handleOne(queue.replace(RETRY_KEY_PREFIX, ""), json);
        }
    }

    /**
     * SCAN 유틸리티
     * <P>
     * pattern과 count로 키를 검색한다.
     * </P>
     * 
     * @PARAM pattern 매칭 패턴
     * @PARAM count 힌트 카운트
     * @RETURN 키 목록
     */
    private List<String> scan(String pattern, int count) {
        return redisTemplate.execute((RedisCallback<List<String>>) connection -> {
            List<String> keys = new ArrayList<>();
            try (Cursor<byte[]> cursor = connection.keyCommands()
                    .scan(ScanOptions.scanOptions().match(pattern).count(count).build())) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                log.error("SCAN error: {}", e.toString());
            }
            return keys;
        });
    }

}
