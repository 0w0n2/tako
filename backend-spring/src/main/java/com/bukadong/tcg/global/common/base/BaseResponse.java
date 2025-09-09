package com.bukadong.tcg.global.common.base;

import com.bukadong.tcg.global.support.TypeCaster;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import static com.bukadong.tcg.global.common.base.BaseResponseStatus.SUCCESS;


public record BaseResponse<T>(
        HttpStatusCode httpStatus,
        Boolean isSuccess,
        String message,
        int code,
        T result
) {
    /**
     * 필요값 : Http상태코드, 성공여부, 메시지, 에러코드, 결과값
     * 1. Return 객체가 필요한 경우 -> 성공
     * 2. Return 객체가 필요 없는 경우 -> 성공
     * 3. 요청에 실패한 경우
     */

    // 타입 명확히 지정한 static 메서드 (컨트롤러 전용)
    /* 성공 응답 (결과 데이터 포함) */
    public static <T> BaseResponse<T> onSuccess(T result) {
        return new BaseResponse<>(HttpStatus.OK, true, SUCCESS.getMessage(), SUCCESS.getCode(), result);
    }

    /* 성공 응답 (결과 데이터 없음) */
    public static BaseResponse<Void> onSuccess() {
        return new BaseResponse<>(HttpStatus.OK, true, SUCCESS.getMessage(), SUCCESS.getCode(), null);
    }

    /* 실패 응답 (BaseResponseStatus 사용) */
    public static <T> BaseResponse<T> onFailure(BaseResponseStatus status) {
        return new BaseResponse<>(status.getHttpStatusCode(), status.isSuccess(), status.getMessage(), status.getCode(), null);
    }

    /* 실패 응답 (커스텀 메시지 사용) */
    public static <T> BaseResponse<T> onFailure(BaseResponseStatus status, String message) {
        return new BaseResponse<>(status.getHttpStatusCode(), status.isSuccess(), message, status.getCode(), null);
    }
    
    public BaseResponse(T result) {
        this(HttpStatus.OK, true, SUCCESS.getMessage(), SUCCESS.getCode(), result);
    }

    public BaseResponse() {
        this(HttpStatus.OK, true, SUCCESS.getMessage(), SUCCESS.getCode(), null);
    }

    public BaseResponse(BaseResponseStatus status) {
        this(status.getHttpStatusCode(), status.isSuccess(), status.getMessage(), status.getCode(), null);
    }

    public BaseResponse(BaseResponseStatus status, String message) {
        this(status.getHttpStatusCode(), status.isSuccess(), message, status.getCode(), null);
    }
}