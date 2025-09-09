package com.bukadong.tcg.global.constant;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String AUTHORITIES_CLAIM = "auth";

    public static final String BLACKLIST_ACCESS_PREFIX = "blacklist-at:";
    public static final String BLACKLIST_REFRESH_PREFIX = "blacklist-rt:";
}
