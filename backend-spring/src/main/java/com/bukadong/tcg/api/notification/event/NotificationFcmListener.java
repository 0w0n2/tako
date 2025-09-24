package com.bukadong.tcg.api.notification.event;

import com.bukadong.tcg.api.fcm.service.FcmPushService;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.api.notification.entity.NotificationSetting;
import com.bukadong.tcg.api.notification.entity.NotificationType;
import com.bukadong.tcg.api.notification.entity.NotificationTypeCode;
import com.bukadong.tcg.api.notification.repository.NotificationSettingRepository;
import com.bukadong.tcg.api.notification.repository.NotificationTypeRepository;
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
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final MemberRepository memberRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(NotificationCreatedEvent event) {
        if (event.memberId() == null) {
            return; // 수신자 없는 경우 스킵
        }
        // 1) 사용자 push 설정 확인 (Notification 은 이미 저장되었으므로 여기서는 push 여부만 제어)
        try {
            NotificationTypeCode code = event.typeCode();
            // NotificationType 이 누락되어도 동적으로 생성 (seed 누락 방어)
            NotificationType type = notificationTypeRepository.findByCode(code)
                    .orElseGet(() -> notificationTypeRepository.save(NotificationType.of(code, code.name())));

            // 멤버 참조 (존재 안 하면 push 스킵)
            Member member = memberRepository.findById(event.memberId()).orElse(null);
            if (member == null) {
                log.warn("Skip push - member not found memberId={} notificationId={}", event.memberId(),
                        event.notificationId());
                return;
            }

            NotificationSetting setting = notificationSettingRepository.findByMemberAndNotificationType(member, type)
                    .orElse(null);
            if (setting == null) {
                // 기본 정책: 최초 생성 시 enabled = true (사용자가 별도 조정하지 않았다면 ON 간주)
                try {
                    notificationSettingRepository.save(
                            NotificationSetting.builder().member(member).notificationType(type).enabled(true).build());
                } catch (org.springframework.dao.DataIntegrityViolationException dup) {
                    // 동시경합으로 인한 중복은 무시
                }
            } else if (Boolean.FALSE.equals(setting.getEnabled())) {
                // 명시적으로 OFF 인 경우 push 스킵
                log.debug("Skip push by user setting: memberId={} code={} notificationId={}", event.memberId(), code,
                        event.notificationId());
                return;
            }
        } catch (Exception settingEx) {
            // 설정 로직 실패 시 푸시 자체는 차단하지 않고 로그만 (정책적으로 바꾸려면 return)
            log.warn("Push setting evaluation failed memberId={} notificationId={} err={}", event.memberId(),
                    event.notificationId(), settingEx.getMessage());
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
