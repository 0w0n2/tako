package com.bukadong.tcg.global.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken, int time) {
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(time)
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString()); // TODO-SECURITY: 추후 RT 쿠키 헤더 노출 제거 필요
    }
}
