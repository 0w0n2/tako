package com.bukadong.tcg.global.util;

import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.IOException;

public final class ErrorResponseUtils {
    private static final Logger log = LoggerFactory.getLogger(ErrorResponseUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ErrorResponseUtils() {
    }

    /* 에러 응답을 설정 */
    public static void setErrorResponse(HttpServletResponse response, BaseResponseStatus status) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        response.setStatus(status.getHttpStatusCode().value());
        try {
            BaseResponse<Void> baseResponse = new BaseResponse<>(status);
            String responseBody = objectMapper.writeValueAsString(baseResponse);
            response.getWriter().write(responseBody);
        } catch (IOException e) {
            log.error("Failed to write error response to HttpServletResponse", e);
        }
    }
}
