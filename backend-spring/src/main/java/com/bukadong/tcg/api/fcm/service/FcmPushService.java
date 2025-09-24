package com.bukadong.tcg.api.fcm.service;

import com.bukadong.tcg.api.fcm.entity.FcmToken;
import com.bukadong.tcg.api.fcm.repository.FcmTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmPushService {

    private final FcmTokenRepository fcmTokenRepository;

    /**
     * 단일 회원에게 FCM 전송 (등록된 모든 기기)
     */
    @Transactional(readOnly = true)
    public int sendToMember(Long memberId, String title, String body) {
        List<FcmToken> tokens = fcmTokenRepository.findByMember_Id(memberId);
        int success = 0;
        for (FcmToken t : tokens) {
            try {
                Message message = Message.builder().setToken(t.getToken())
                        .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                        .putData("memberId", String.valueOf(memberId)).build();
                String id = FirebaseMessaging.getInstance().send(message);
                log.debug("FCM sent: token={}, msgId={}", t.getToken(), id);
                success++;
            } catch (Exception e) {
                log.warn("FCM send failed token={}: {}", t.getToken(), e.getMessage());
            }
        }
        return success;
    }

    /**
     * Raw 토큰 단건 전송 (회원 연관 무시) - Swagger 테스트 용도
     */
    @Transactional(readOnly = true)
    public boolean sendRawToken(String token, String title, String body) {
        try {
            Message message = Message.builder().setToken(token)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build()).build();
            String id = FirebaseMessaging.getInstance().send(message);
            log.debug("FCM raw sent: token={}, msgId={}", token, id);
            return true;
        } catch (Exception e) {
            log.warn("FCM raw send failed token={}: {}", token, e.getMessage());
            return false;
        }
    }
}
