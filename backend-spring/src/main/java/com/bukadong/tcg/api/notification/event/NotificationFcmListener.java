package com.bukadong.tcg.api.notification.event;

import com.bukadong.tcg.api.fcm.service.FcmPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

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
            int sent = fcmPushService.sendToMember(event.memberId(), event.title(), event.message());
            log.debug("NotificationFcmListener sent push: notificationId={}, memberId={}, successCount={}",
                    event.notificationId(), event.memberId(), sent);
        } catch (Exception e) {
            // 비즈니스 트랜잭션과 분리: 실패 로깅만
            log.warn("NotificationFcmListener push failed: notificationId={}, memberId={}, err={}",
                    event.notificationId(), event.memberId(), e.getMessage());
        }
    }
}
