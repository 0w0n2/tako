package com.bukadong.tcg.api.notification.event;

import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.api.notification.dto.NotificationEvent;
import com.bukadong.tcg.api.notification.sse.UserNotificationSseService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * NotificationCreatedEvent 를 수신하여 SSE 알림을 전송하는 리스너
 */
@Component
@RequiredArgsConstructor
public class NotificationSseListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationSseListener.class);

    private final UserNotificationSseService sseService;
    private final MemberRepository memberRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(NotificationCreatedEvent event) {
        if (event.memberId() == null)
            return;
        try {
            Member member = memberRepository.findById(event.memberId()).orElse(null);
            if (member == null)
                return;
            String uuid = member.getUuid();

            NotificationEvent payload = NotificationEvent.builder()
                    .type(event.typeCode() == null ? "SYSTEM" : event.typeCode().name()).title(event.title())
                    .message(event.message()).causeId(event.causeId()).build();

            // SSE 이벤트명은 일반화된 'notification'으로 송신, 프론트는 payload.type+causeId로 라우팅
            sseService.sendToUser(uuid, "notification", payload);
        } catch (Exception ex) {
            log.warn("NotificationSseListener failed to send SSE: notificationId={} memberId={} err={}",
                    event.notificationId(), event.memberId(), ex.getMessage());
        }
    }
}
