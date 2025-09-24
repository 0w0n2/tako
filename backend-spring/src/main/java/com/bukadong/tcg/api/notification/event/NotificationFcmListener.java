package com.bukadong.tcg.api.notification.event;

import com.bukadong.tcg.api.fcm.service.FcmPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import java.util.HashMap;
import java.util.Map;
import com.bukadong.tcg.api.notification.util.NotificationPushPayloadMapper;

/**
 * NotificationCreatedEvent 를 수신하여 FCM 푸시를 전송하는 리스너
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationFcmListener {

    private final FcmPushService fcmPushService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(NotificationCreatedEvent event) {
        if (event.memberId() == null) {
            return; // 수신자 없는 경우 스킵
        }
        try {
            // SW 규격에 맞춘 data payload 구성
            Map<String, String> data = new HashMap<>();
            // SW 에 내려보낼 상위 그룹(type) 과 원래 코드(originalType)
            data.put("type", NotificationPushPayloadMapper.mapGroup(event.typeCode()));
            data.put("originalType", event.typeCode().name());
            if (event.notificationId() != null)
                data.put("notificationId", String.valueOf(event.notificationId()));
            if (event.causeId() != null)
                data.put("causeId", String.valueOf(event.causeId()));
            // tag 기본: 그룹 + causeId
            data.put("tag", NotificationPushPayloadMapper.buildTag(event.typeCode(), event.causeId()));
            // 필요 시 renotify 정책 (여기서는 동일 tag 재도착 시 시스템 기본 동작 유지)
            // data.put("renotify", "true"); // 활성화 원하면 주석 해제

            String path = (event.targetUrl() == null || event.targetUrl().isBlank()) ? "/" : event.targetUrl();

            int sent = fcmPushService.sendToMemberWithMeta(event.memberId(), event.title(), event.message(), path,
                    data);
            log.debug("NotificationFcmListener sent push: notificationId={}, memberId={}, successCount={}",
                    event.notificationId(), event.memberId(), sent);
        } catch (Exception e) {
            // 비즈니스 트랜잭션과 분리: 실패 로깅만
            log.warn("NotificationFcmListener push failed: notificationId={}, memberId={}, err={}",
                    event.notificationId(), event.memberId(), e.getMessage());
        }
    }
}
