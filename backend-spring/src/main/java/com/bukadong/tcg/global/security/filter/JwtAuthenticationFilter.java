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
import lombok.extern.slf4j.Slf4j;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
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
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final TokenBlackListService tokenBlackListService;
    private final SecurityWhitelistProperties securityWhitelistProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    // private final Logger logger =
    // LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        /* 인증이 필요한 요청에 대해서만 검사 */
        if (!isPermitAll(method, uri)) {
            String token = tokenProvider.getTokenFromRequest(request);
            // logger.info("Requested URI: {}, Method: {}, Token: {}", uri, method, token !=
            // null ? "Present" : "Absent");

            if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                if (!tokenBlackListService.isBlacklistAccessToken(token)) { // 블랙리스트 검증
                    Authentication authentication = tokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    // logger.info("Authentication set for user: {}", authentication.getName());
                } else {
                    // logger.warn("Token is blacklisted: {}", token);
                    throw new BaseException(INVALID_JWT_TOKEN);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPermitAll(String method, String uri) {
        return securityWhitelistProperties.getParsedWhitelist().entrySet().stream().anyMatch(entry -> {
            String httpMethodFromConfig = entry.getKey().name();
            boolean methodMatches = httpMethodFromConfig.equalsIgnoreCase(method);

            if (!methodMatches) {
                // logger.info("Method does not match: {}", httpMethodFromConfig);
                return false;
            }
            // logger.info("Checking URI against patterns: {} for method: {}",
            // entry.getValue(), method);
            return entry.getValue().stream().anyMatch(pattern -> antPathMatcher.match(pattern, uri));

        });
    }
}
