package com.bukadong.tcg.global.common.exception;

import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @RestControllerAdvice 어노테이션을 통해 프로젝트 전역의 Controller 단에서 발생하는 예외를 처리하는 클래스
 */
@RestControllerAdvice
@Slf4j
public class BaseExceptionHandler {

    @ExceptionHandler(BaseException.class)
    protected BaseResponse<Void> baseError(BaseException e) {
        log.error("BaseException -> {}({})", e.getStatus(), e.getStatus().getMessage(), e);
        return new BaseResponse<>(e.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected BaseResponse<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getFieldErrors().get(0).getDefaultMessage();
        log.error("MethodArgumentNotValidException -> {}", message);
        return BaseResponse.onFailure(BaseResponseStatus.INVALID_PARAMETER, message);
    }

    @ExceptionHandler(RuntimeException.class)
    protected BaseResponse<Void> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException -> {}", e.getMessage(), e);
        return BaseResponse.onFailure(BaseResponseStatus.INTERNAL_SERVER_ERROR);
    }
}