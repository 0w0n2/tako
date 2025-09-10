package com.bukadong.tcg.global.security.filter;

import static com.bukadong.tcg.global.common.base.BaseResponseStatus.*;

import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.properties.SecurityWhitelistProperties;
import com.bukadong.tcg.global.security.provider.TokenBlackListService;
import com.bukadong.tcg.global.security.provider.TokenProvider;
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

    private final TokenProvider tokenProvider;
    private final TokenBlackListService tokenBlackListService;
    private final SecurityWhitelistProperties whitelistProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        return whitelistProperties.getParsedWhitelist().entrySet().stream()
                .anyMatch(entry -> {
                    boolean methodMatches = entry.getKey().name().equalsIgnoreCase(method);
                    boolean pathMatches = entry.getValue().stream()
                            .anyMatch(pattern -> pathMatcher.match(pattern, uri));
                    return methodMatches && pathMatches;
                });
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = tokenProvider.getTokenFromRequest(request);

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            if (!tokenBlackListService.isBlacklistAccessToken(token)) {   // 블랙리스트 검증
                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                throw new BaseException(INVALID_JWT_TOKEN);
            }
        }
        filterChain.doFilter(request, response);
    }
}
