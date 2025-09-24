package com.bukadong.tcg.api.notification.event;

import com.bukadong.tcg.api.notification.entity.NotificationTypeCode;

/**
 * 알림 생성 이벤트
 * <p>
 * 트랜잭션 내에서 알림 엔티티가 저장된 직후 발행되고, AFTER_COMMIT 단계의 리스너에서 FCM 전송을 수행한다. push 실패가
 * 비즈니스 트랜잭션을 롤백시키지 않도록 도메인 저장과 분리한다.
 * </p>
 */
public record NotificationCreatedEvent(Long notificationId, Long memberId, NotificationTypeCode typeCode, Long causeId,
        String title, String message) {
}
