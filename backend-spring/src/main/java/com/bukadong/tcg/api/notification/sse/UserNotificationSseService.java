package com.bukadong.tcg.api.notification.sse;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 로그인 유저별 알림 전송을 위한 SSE 서비스. - 키는 memberUuid (String) - subscribe: 연결 및 초기 핑 전송
 * - sendToUser: 특정 유저에게 알림 전송
 */
@Service
public class UserNotificationSseService {

    private static final Logger log = LoggerFactory.getLogger(UserNotificationSseService.class);

    // uuid -> emitters (브라우저 탭/기기당 여러 연결 허용)
    private final Map<String, Set<SseEmitter>> perUserEmitters = new ConcurrentHashMap<>();
    private final Map<SseEmitter, Integer> failureCounts = new ConcurrentHashMap<>();

    @Value("${notification.sse.timeout-ms:600000}") // 기본 10분
    private long defaultTimeoutMs;

    @Value("${notification.sse.heartbeat-ms:20000}")
    private long heartbeatMs;

    public SseEmitter subscribe(String memberUuid) {
        SseEmitter emitter = new SseEmitter(defaultTimeoutMs);
        attachLifecycle(memberUuid, emitter);
        perUserEmitters.computeIfAbsent(memberUuid, k -> new CopyOnWriteArraySet<>()).add(emitter);

        // 연결 직후 클라이언트가 구독 성공을 인지할 수 있도록 1회 ping
        sendEvent(emitter, "connected", Map.of("ts", Instant.now().toEpochMilli()));
        return emitter;
    }

    public void sendToUser(String memberUuid, String type, Object payload) {
        Set<SseEmitter> set = perUserEmitters.get(memberUuid);
        if (set == null || set.isEmpty())
            return;
        for (SseEmitter e : set)
            sendEvent(e, type, payload);
    }

    public int getActiveConnectionCount(String memberUuid) {
        Set<SseEmitter> set = perUserEmitters.get(memberUuid);
        return set == null ? 0 : set.size();
    }

    @Scheduled(fixedRateString = "${notification.sse.heartbeat-ms:20000}")
    public void heartbeat() {
        long now = Instant.now().toEpochMilli();
        for (Set<SseEmitter> set : perUserEmitters.values()) {
            for (SseEmitter e : set)
                sendEvent(e, "heartbeat", Map.of("ts", now));
        }
    }

    private void attachLifecycle(String memberUuid, SseEmitter emitter) {
        emitter.onTimeout(() -> {
            log.debug("[SSE][user={}] timeout", memberUuid);
            removeEmitter(memberUuid, emitter);
        });
        emitter.onCompletion(() -> {
            log.debug("[SSE][user={}] completed", memberUuid);
            removeEmitter(memberUuid, emitter);
        });
        emitter.onError(ex -> {
            log.debug("[SSE][user={}] error: {}", memberUuid, ex.getMessage());
            removeEmitter(memberUuid, emitter);
        });
    }

    private void removeEmitter(String memberUuid, SseEmitter emitter) {
        Set<SseEmitter> set = perUserEmitters.get(memberUuid);
        if (set != null) {
            set.remove(emitter);
            if (set.isEmpty())
                perUserEmitters.remove(memberUuid);
        }
        failureCounts.remove(emitter);
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) {
        try {
            SseEmitter.SseEventBuilder evt = SseEmitter.event().name(name).data(data, MediaType.APPLICATION_JSON);
            emitter.send(evt);
            failureCounts.remove(emitter);
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
            // 연결 소유자를 모르면 전체 sweep 비용이 있으나, 이 서비스는 remove 시점에만 memberUuid를 전달하여 제거
            // 여기서는 단순히 complete 처리만 시도
            try {
                // client가 이미 종료된 경우 complete() 호출은 실패할 수 있으나 추가 조치 불필요
                emitter.complete();
            } catch (Exception ignore) {
                // no-op: 연결 정리 목적의 best-effort
            }
        }
    }
}
