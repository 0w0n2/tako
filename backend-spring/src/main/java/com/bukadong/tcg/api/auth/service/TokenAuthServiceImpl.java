package com.bukadong.tcg.api.auth.service;

import static com.bukadong.tcg.global.common.base.BaseResponseStatus.*;
import com.bukadong.tcg.global.common.exception.BaseException;
import static com.bukadong.tcg.global.constant.SecurityConstants.*;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import com.bukadong.tcg.global.security.dto.JwtToken;
import com.bukadong.tcg.global.security.provider.TokenBlackListService;
import com.bukadong.tcg.global.security.provider.TokenProvider;
import com.bukadong.tcg.global.security.provider.TokenService;
import com.bukadong.tcg.global.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenAuthServiceImpl implements TokenAuthService {

    private final AuthenticationProvider authenticationProvider;
    private final TokenService tokenService;
    private final TokenBlackListService tokenBlackListService;

    private final CookieUtils cookieUtils;
    private final TokenProvider tokenProvider;

    @Value("${security.jwt.expire-time.refresh-token}")
    private Duration refreshExpiration;

    @Override
    public CustomUserDetails authenticate(String username, String password) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationProvider.authenticate(authenticationToken);
            return (CustomUserDetails) authentication.getPrincipal();
        } catch (UsernameNotFoundException | DisabledException e) { // 존재하지 않거나, 삭제된 회원
            throw new BaseException(NO_EXIST_USER);
        } catch (BadCredentialsException e) {   // 비밀번호 불일치
            throw new BaseException(PASSWORD_MATCH_FAILED);
        } catch (AuthenticationException e) {
            throw new BaseException(AUTHENTICATION_REQUIRED);
        }
    }

    @Override
    public void issueJwt(CustomUserDetails userDetails, HttpServletResponse response) {
        JwtToken jwtToken = tokenService.generateToken(userDetails);
        response.addHeader(HttpHeaders.AUTHORIZATION, GRANT_TYPE + jwtToken.accessToken());
        cookieUtils.setRefreshTokenCookie(response, jwtToken.refreshToken(), (int) refreshExpiration.getSeconds());
    }

    @Override
    public void signOut(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = tokenProvider.getTokenFromRequest(request);
        String memberUuid = tokenProvider.getSubjectFromToken(accessToken);

        tokenService.deleteRefreshToken(memberUuid);
        tokenBlackListService.addBlacklistAccessToken(accessToken);
        cookieUtils.setRefreshTokenCookie(response, "", 0);
    }

    @Override
    public void refreshAccessToken(HttpServletResponse response, String refreshToken) {
        JwtToken newTokens = tokenService.refresh(refreshToken);
        tokenBlackListService.addBlacklistRefreshToken(refreshToken);
        response.addHeader(HttpHeaders.AUTHORIZATION, GRANT_TYPE + newTokens.accessToken());
        cookieUtils.setRefreshTokenCookie(response, newTokens.refreshToken(), (int) refreshExpiration.getSeconds());
    }
}
