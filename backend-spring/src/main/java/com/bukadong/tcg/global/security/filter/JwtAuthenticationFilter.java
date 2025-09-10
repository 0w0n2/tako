package com.bukadong.tcg.global.security.filter;

import static com.bukadong.tcg.global.common.base.BaseResponseStatus.*;

import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.properties.SecurityWhitelistProperties;
import com.bukadong.tcg.global.security.service.JwtTokenBlackListService;
import com.bukadong.tcg.global.util.JwtTokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 토큰을 검증하여 사용자를 인증하는 Spring Security 필터
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtils jwtTokenUtils;
    private final JwtTokenBlackListService jwtTokenBlackListService;
    private final SecurityWhitelistProperties whitelistProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestMethod = request.getMethod();
        String requestUri = request.getRequestURI();

        // permitAll()이 아닌 경로에 대해서 검증
        if (!isPermitAll(requestMethod, requestUri)) {
            String token = jwtTokenUtils.getTokenFromRequest(request);

            if (StringUtils.hasText(token)) {
                if (!jwtTokenUtils.validateToken(token)) {  // 토큰 유효성 검증
                    throw new BaseException(INVALID_JWT_TOKEN);
                }
                if (jwtTokenBlackListService.isBlacklistAccessToken(token)) {   // 블랙리스트 검증
                    throw new BaseException(INVALID_JWT_TOKEN);
                }

                Authentication authentication = jwtTokenUtils.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {    // 보호된 경로인데 토큰이 없는 경우
                throw new BaseException(AUTHENTICATION_REQUIRED);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPermitAll(String method, String uri) {
        return whitelistProperties.getParsedWhitelist().entrySet().stream()
                .anyMatch(entry -> entry.getKey().matches(method) &&
                        entry.getValue().stream().anyMatch(pattern -> pathMatches(pattern, uri)));
    }

    private boolean pathMatches(String pattern, String uri) {
        return this.antPathMatcher.match(pattern, uri);
    }
}
