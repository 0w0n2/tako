package com.bukadong.tcg.api.fcm.service;

import com.bukadong.tcg.api.fcm.entity.FcmToken;
import com.bukadong.tcg.api.fcm.repository.FcmTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmPushService {

    private final FcmTokenRepository fcmTokenRepository;
    private final PushMessageFactory pushMessageFactory;

    /**
     * 단일 회원에게 FCM 전송 (등록된 모든 기기)
     */
    // 무효 토큰을 삭제할 수 있도록 readOnly 제거
    @Transactional
    public int sendToMember(Long memberId, String title, String body) {
        List<FcmToken> tokens = fcmTokenRepository.findByMember_Id(memberId);
        int success = 0;
        for (FcmToken t : tokens) {
            try {
                Message message = pushMessageFactory.build(t.getToken(), memberId, title, body, "/", null, null);
                String id = FirebaseMessaging.getInstance().send(message);
                log.debug("FCM sent: token={}, msgId={}", t.getToken(), id);
                success++;
            } catch (FirebaseMessagingException e) {
                MessagingErrorCode code = e.getMessagingErrorCode();
                if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT
                        || code == MessagingErrorCode.SENDER_ID_MISMATCH) {
                    // 더 이상 유효하지 않은 토큰 -> DB에서 제거
                    try {
                        fcmTokenRepository.deleteByToken(t.getToken());
                        log.info("Removed invalid FCM token={} reason={}", t.getToken(), code);
                    } catch (Exception repoEx) {
                        log.warn("Failed to remove invalid FCM token={} reason={} err={}", t.getToken(), code,
                                repoEx.getMessage());
                    }
                } else {
                    log.warn("FCM send failed token={} code={} msg={}", t.getToken(), code, e.getMessage());
                }
            } catch (Exception e) {
                log.warn("FCM send failed (unexpected) token={} msg={}", t.getToken(), e.getMessage());
            }
        }
        return success;
    }

    /**
     * Raw 토큰 단건 전송 (회원 연관 무시) - Swagger 테스트 용도
     */
    @Transactional
    public boolean sendRawToken(String token, String title, String body) {
        try {
            Message message = pushMessageFactory.build(token, null, title, body, "/", null, null);
            String id = FirebaseMessaging.getInstance().send(message);
            log.debug("FCM raw sent: token={}, msgId={}", token, id);
            return true;
        } catch (FirebaseMessagingException e) {
            var code = e.getMessagingErrorCode();
            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT
                    || code == MessagingErrorCode.SENDER_ID_MISMATCH) {
                // raw token 전송 테스트 중이면 repository 에 없을 수도 있으니 단순 로그
                log.warn("FCM raw send failed invalid token={} code={} msg={}", token, code, e.getMessage());
            } else {
                log.warn("FCM raw send failed token={} code={} msg={}", token, code, e.getMessage());
            }
            return false;
        } catch (Exception e) {
            log.warn("FCM raw send failed (unexpected) token={} msg={}", token, e.getMessage());
            return false;
        }
    }

    /**
     * (신규) 메타데이터/경로 포함 전송 - Notification 이벤트 연동용
     */
    @Transactional
    public int sendToMemberWithMeta(Long memberId, String title, String body, String path,
            Map<String, String> extraData) {
        List<FcmToken> tokens = fcmTokenRepository.findByMember_Id(memberId);
        int success = 0;
        for (FcmToken t : tokens) {
            try {
                Message message = pushMessageFactory.build(t.getToken(), memberId, title, body, path, extraData, null);
                String id = FirebaseMessaging.getInstance().send(message);
                log.debug("FCM sent(meta): token={}, msgId={}", t.getToken(), id);
                success++;
            } catch (FirebaseMessagingException e) {
                MessagingErrorCode code = e.getMessagingErrorCode();
                if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT
                        || code == MessagingErrorCode.SENDER_ID_MISMATCH) {
                    try {
                        fcmTokenRepository.deleteByToken(t.getToken());
                        log.info("Removed invalid FCM token={} reason={}", t.getToken(), code);
                    } catch (Exception repoEx) {
                        log.warn("Failed to remove invalid FCM token={} reason={} err={}", t.getToken(), code,
                                repoEx.getMessage());
                    }
                } else {
                    log.warn("FCM send(meta) failed token={} code={} msg={}", t.getToken(), code, e.getMessage());
                }
            } catch (Exception e) {
                log.warn("FCM send(meta) failed (unexpected) token={} msg={}", t.getToken(), e.getMessage());
            }
        }
        return success;
    }

}
