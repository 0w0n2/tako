package com.bukadong.tcg.api.bid.service;

/**
 * 재시도가 의미 없는 영구 실패를 나타내는 런타임 예외.
 * <P>
 * 컨슈머는 이 예외를 받으면 즉시 dead-letter 큐로 라우팅한다.
 * </P>
 */
public class NonRetryableException extends RuntimeException {
    public NonRetryableException(String message) {
        super(message);
    }

    public NonRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
