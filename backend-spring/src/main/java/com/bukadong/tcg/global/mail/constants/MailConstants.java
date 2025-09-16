package com.bukadong.tcg.global.mail.constants;

import java.time.Duration;

/**
 * 메일 관련 상수 정의
 */
public final class MailConstants {

    private MailConstants() {
    }

    public static final String MAIN_URL_KEY = "mainUrl";
    public static final String LOGO_IMAGE_URL_KEY = "logoImageUrl";
    public static final String CODE_KEY = "code";

    public static final String MAIL_VERIFICATION_CODE_PREFIX = "mail-verification-code:";
    public static final Duration mailCodeExpMin = Duration.ofMinutes(5);
}
