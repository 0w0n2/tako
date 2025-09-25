package com.bukadong.tcg.api.fcm.service;

import com.bukadong.tcg.api.fcm.config.PushProperties;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 플랫폼/채널(PWA Web, Android, iOS)을 모두 고려한 FCM Message 빌더 팩토리. - 웹: WebpushConfig
 * + icon/badge + click_action - Android: channelId, priority, data 포함 - iOS:
 * ApnsConfig (sound, badge optional)
 */
@Component
@RequiredArgsConstructor
public class PushMessageFactory {

    private final PushProperties props;

    public Message build(String token, Long memberId, String title, String body, String path,
            Map<String, String> extraData, Integer iosBadge) {
        if (path == null)
            path = "/";
        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("body", body);
        data.put("click_action", path);
        if (memberId != null)
            data.put("memberId", String.valueOf(memberId));
        if (extraData != null)
            data.putAll(extraData);

        Message.Builder builder = Message.builder().setToken(token)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build());

        // Web (PWA) 설정
        if (props.getWebIcon() != null) {
            builder.setWebpushConfig(
                    WebpushConfig.builder()
                            .setNotification(WebpushNotification.builder().setIcon(props.getWebIcon())
                                    .setBadge(props.getWebBadge()).putCustomData("click_action", path).build())
                            .build());
        }

        // Android 설정
        builder.setAndroidConfig(AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder().setChannelId(props.getAndroidChannelId())
                        .setClickAction("OPEN_MAIN") // 앱 PendingIntent 필터 액션과 매칭되도록 조정
                        .build())
                .build());

        // iOS 설정 (badge 지정 시)
        Aps.Builder apsBuilder = Aps.builder().setSound(props.getIosSound());
        if (iosBadge != null) {
            apsBuilder.setBadge(iosBadge);
        }
        builder.setApnsConfig(ApnsConfig.builder().setAps(apsBuilder.build()).build());

        data.forEach(builder::putData);
        return builder.build();
    }
}