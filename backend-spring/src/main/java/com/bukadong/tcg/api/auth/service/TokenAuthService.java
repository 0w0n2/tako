package com.bukadong.tcg.api.auth.service;

import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenAuthService {

    CustomUserDetails authenticate(String username, String password);

    void issueJwt(CustomUserDetails userDetails, HttpServletResponse response);

    void signOut(HttpServletRequest request, HttpServletResponse response);

    void refreshAccessToken(HttpServletResponse response, String refreshToken);
}
