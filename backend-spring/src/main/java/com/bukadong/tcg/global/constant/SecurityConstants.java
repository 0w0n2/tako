package com.bukadong.tcg.global.constant;

/**
 * Security 관련 상수 정의
 */
public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String AUTHORITIES_CLAIM = "auth";

    public static final String BLACKLIST_ACCESS_PREFIX = "jwt-blacklist-at:";
    public static final String BLACKLIST_REFRESH_PREFIX = "jwt-blacklist-rt:";
}
