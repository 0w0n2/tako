package com.bukadong.tcg.global.security.filter;

import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.util.ErrorResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Spring Security 필터 체인에서 발생하는 BaseException 담당 처리 필터
 */
@Slf4j
@Component
public class BaseExceptionHandlerFilter extends OncePerRequestFilter { // 매 요청마다 한 번만 실행되는 필터 클래스 상속

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response); // 다음 필터 또는 실제 서블릿을 실행
        } catch (BaseException e) { // 커스텀 예외(BaseException) 발생 시 처리
            log.warn("BaseException caught in filter chain: Status={}, Message={}, RequestURI={}", e.getStatus(),
                    e.getStatus().getMessage(), request.getRequestURI());
            ErrorResponseUtils.setErrorResponse(response, e.getStatus());
        }
    }
}