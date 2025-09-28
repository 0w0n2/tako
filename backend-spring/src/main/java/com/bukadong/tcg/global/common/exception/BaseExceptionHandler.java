package com.bukadong.tcg.global.common.exception;

import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.http.HttpStatus;

/**
 * @RestControllerAdvice 어노테이션을 통해 프로젝트 전역의 Controller 단에서 발생하는 예외를 처리하는 클래스
 */
@RestControllerAdvice
public class BaseExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(BaseExceptionHandler.class);

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

    @ExceptionHandler({ MaxUploadSizeExceededException.class, MultipartException.class })
    public ResponseEntity<BaseResponse<Void>> handleMaxUploadSize(Exception ex) {
        log.error("Multipart size exceeded: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE) // 413
                .body(BaseResponse.onFailure(BaseResponseStatus.PAYLOAD_TOO_LARGE));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalState(IllegalStateException ex) {
        String msg = ex.getMessage();
        if (msg != null && msg.contains("exceeds its maximum permitted size")) {
            log.error("Multipart size exceeded (IllegalState): {}", msg);
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(BaseResponse.onFailure(BaseResponseStatus.PAYLOAD_TOO_LARGE));
        }
        throw ex; // 업로드 초과 케이스가 아니면 기존 흐름 유지
    }

    // @RestControllerAdvice
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<BaseResponse<Void>> handleMissingPart(MissingServletRequestPartException ex) {
        // files 없으면 AUCTION_NO_MEDIA, metadata 없으면 INVALID_PARAMETER 등 상황에 맞게 매핑
        BaseResponseStatus status = "files".equalsIgnoreCase(ex.getRequestPartName())
                ? BaseResponseStatus.AUCTION_NO_MEDIA
                : BaseResponseStatus.INVALID_PARAMETER;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.onFailure(status));
    }

}