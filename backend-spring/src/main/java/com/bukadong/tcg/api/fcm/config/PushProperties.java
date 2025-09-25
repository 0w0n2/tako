package com.bukadong.tcg.api.fcm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "push")
public record PushProperties(
        /** 웹 알림 main icon (192x192) 절대 또는 상대 경로 */
        String webIcon,

        /** 웹 알림 badge icon (72x72 권장) */
        String webBadge,

        /** 클릭 시 열 기본 경로 (예: /) */
        String clickBaseUrl,

        /** Android 기본 채널 ID (앱에 미리 생성되어 있어야 함) */
        String androidChannelId,

        /**
         * iOS 사운드 (default 또는 Bundle 내 파일명)
         */
        String iosSound
) {
}