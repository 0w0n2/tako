package com.bukadong.tcg.global.common.exception;

import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class BaseExceptionHandler {

    /**
     * 발생한 예외 처리
     */
    @ExceptionHandler(BaseException.class)
    protected BaseResponse<Void> baseError(BaseException e) {
        log.error("BaseException -> {}({})", e.getStatus(), e.getStatus().getMessage(), e);
        return new BaseResponse<>(e.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected BaseResponse<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getFieldErrors().get(0).getDefaultMessage();
        log.error("MethodArgumentNotValidException -> {}", message);
        return new BaseResponse<>(BaseResponseStatus.INVALID_PARAMETER, message);
    }

    @ExceptionHandler(RuntimeException.class)
    protected BaseResponse<Void> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException -> {}", e.getMessage(), e);
        return new BaseResponse<>(BaseResponseStatus.INTERNAL_SERVER_ERROR);
    }
}