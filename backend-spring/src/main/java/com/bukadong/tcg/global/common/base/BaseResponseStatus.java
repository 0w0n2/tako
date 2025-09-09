package com.bukadong.tcg.global.common.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

// https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml
// following http status code standard from above

@Getter
@AllArgsConstructor
public enum BaseResponseStatus {

    /**
     * 2XX: Success(성공)
     **/
    SUCCESS(HttpStatus.OK, true, 200, "요청에 성공하였습니다."),

    /**
     * 4XX: Client Error(클라이언트 에러)
     */
    DISALLOWED_ACTION(HttpStatus.BAD_REQUEST, false, 400, "올바르지 않은 행위 요청입니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, false, 400, "잘못된 매개변수입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "요청하신 정보를 찾을 수 없습니다."),

    /* 401 UNAUTHORIZED: 인증 실패 */
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, false, 40101, "인증이 필요한 요청입니다."),
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, false, 40102, "유효하지 않은 JWT 토큰입니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, false, 40103, "만료된 JWT 토큰입니다."),
    UNSUPPORTED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, false, 40104, "지원되지 않는 형식의 JWT 토큰입니다."),
    INVALID_TOKEN_CLAIM(HttpStatus.UNAUTHORIZED, false, 40105, "토큰의 클레임 정보가 올바르지 않습니다."),

    /* 403 FORBIDDEN: 인가 실패 (권한 없음) */
    ACCESS_DENIED(HttpStatus.FORBIDDEN, false, 403, "접근 권한이 없습니다."),

    /**
     * 5XX: Server Error(서버 에러)
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "Internal server error"),
    REDIS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "Internal Cache system failure"),

    /**
     * Service Related Errors
     */

    // token
    TOKEN_NOT_VALID(HttpStatus.UNAUTHORIZED, false, 401, "토큰이 유효하지 않습니다."),

    // Users
    DUPLICATED_USER(HttpStatus.CONFLICT, false, 409, "이미 가입된 멤버입니다."),
    FAILED_TO_LOGIN(HttpStatus.UNAUTHORIZED, false, 401, "아이디 또는 패스워드를 다시 확인하세요."),
    DUPLICATED_SOCIAL_USER(HttpStatus.CONFLICT, false, 409, "이미 소셜 연동된 계정입니다."),
    DUPLICATED_SOCIAL_PROVIDER_USER(HttpStatus.CONFLICT, false, 409, "계정에 동일한 플랫폼이 이미 연동되어있습니다"),
    NO_EXIST_USER(HttpStatus.NOT_FOUND, false, 404, "존재하지 않는 멤버 정보입니다."),
    PASSWORD_MATCH_FAILED(HttpStatus.BAD_REQUEST, false, 400, "패스워드를 다시 확인해주세요."),
    NO_SUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, false, 400, "지원하지 않는 플랫폼입니다"),
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT, false, 409, "이미 사용중인 닉네임입니다."),
    SAME_NICKNAME(HttpStatus.CONFLICT, false, 409, "현재 사용중인 닉네임입니다."),
    INVALID_EMAIL_ADDRESS(HttpStatus.BAD_REQUEST, false, 400, "이메일을 다시 확인해주세요."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, false, 400, "닉네임 형식을 맞춰주세요"),
    UPDATE_NICKNAME_FAIL(HttpStatus.BAD_REQUEST, false, 400, "닉네임 업데이트를 실패했습니다."),

    /**
     * Notification
     */
    SSE_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, false, 503, "알림 전송에 실패하였습니다."),
    NO_EXIST_SSE_CONNECTION(HttpStatus.NOT_FOUND, false, 404, "SSE 연결이 존재하지 않습니다."),
    PUSH_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, false, 503, "푸시 알림 전송에 실패하였습니다."),

    /**
     * Image Upload
     */
    IMAGE_FILE_EMPTY(HttpStatus.BAD_REQUEST, false, 400, "이미지 파일이 비어있습니다."),
    IMAGE_FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, false, 400, "이미지 크기는 2MB 이하만 허용됩니다."),
    IMAGE_FILE_TYPE_INVALID(HttpStatus.BAD_REQUEST, false, 400, "이미지 형식만 업로드할 수 있습니다."),

    // CONFLICT 방지 하단에 추가 REFACTOR 부탁해요~
    CATEGORY_BAD_REQUEST(HttpStatus.BAD_REQUEST, false, 400, "카테고리 요청이 올바르지 않습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "카테고리를 찾을 수 없습니다."),
    INVALID_AUCTION_BID_UNIT(HttpStatus.BAD_REQUEST, false, 400, "경매 입찰 단위 변환에 실패하였습니다.");

    private final HttpStatusCode httpStatusCode;
    private final boolean isSuccess;
    private final int code;
    private final String message;

}