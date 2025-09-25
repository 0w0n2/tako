package com.bukadong.tcg.api.fcm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "push")
public class PushProperties {
    /** 웹 알림 main icon (192x192) 절대 또는 상대 경로 */
    private String webIcon;
    /** 웹 알림 badge icon (72x72 권장) */
    private String webBadge;
    /** 클릭 시 열 기본 경로 (예: /) */
    private String clickBaseUrl = "/";
    /** Android 기본 채널 ID (앱에 미리 생성되어 있어야 함) */
    private String androidChannelId = "default-channel";
    /** iOS 사운드 (default 또는 Bundle 내 파일명) */
    private String iosSound = "default";

    public String getWebIcon() {
        return webIcon;
    }

    public String getWebBadge() {
        return webBadge;
    }

    public String getClickBaseUrl() {
        return clickBaseUrl;
    }

    public String getAndroidChannelId() {
        return androidChannelId;
    }

    public String getIosSound() {
        return iosSound;
    }
}