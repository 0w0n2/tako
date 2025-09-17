package com.bukadong.tcg.api.bid.service;

/**
 * 일시적(재시도 가능) 실패 예외
 * <P>
 * DB 락 경합, 일시 네트워크 장애 등 재시도 가치가 있는 경우 사용.
 * </P>
 * 
 * @PARAM message 메시지
 * @RETURN 없음
 */
public class RetryableException extends RuntimeException {
    public RetryableException(String message) {
        super(message);
    }

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}