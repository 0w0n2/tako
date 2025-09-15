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
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, false, 413, "파일 업로드 용량 초과입니다. (파일당/요청합계 제한을 확인하세요)"),

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
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "서버에서 예기치 않은 오류가 발생했습니다."),
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
    NO_EXIST_USER(HttpStatus.NOT_FOUND, false, 404, "존재하지 않는 계정입니다."),
    PASSWORD_MATCH_FAILED(HttpStatus.BAD_REQUEST, false, 400, "비밀번호를 다시 확인해주세요."),
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
    INVALID_AUCTION_BID_UNIT(HttpStatus.BAD_REQUEST, false, 400, "경매 입찰 단위 변환에 실패하였습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, false, 400, "잘못된 요청입니다."),

    /**
     * 600: 멤버 에러
     */
    NICKNAME_GENERATION_FAILED(HttpStatus.CONFLICT, false, 680, "랜덤 닉네임 생성을 실패했습니다."),

    /**
     * 700: AWS 에러
     */
    S3_FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, false, 700, "파일 업로드에 실패했습니다."),

    /**
     * 800: Category Error
     */
    // ====== [추가] Category Domain Errors ======
    CATEGORY_BAD_REQUEST(HttpStatus.BAD_REQUEST, false, 800, "카테고리 요청이 올바르지 않습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, false, 804, "카테고리를 찾을 수 없습니다."),
    CATEGORY_PARENT_NOT_FOUND(HttpStatus.NOT_FOUND, false, 804, "상위 대분류를 찾을 수 없습니다."),
    CATEGORY_MAJOR_NAME_DUPLICATED(HttpStatus.CONFLICT, false, 809, "이미 사용 중인 대분류명입니다."),
    CATEGORY_MEDIUM_NAME_DUPLICATED(HttpStatus.CONFLICT, false, 809, "이미 사용 중인 중분류명입니다."),
    CATEGORY_MAJOR_IN_USE(HttpStatus.CONFLICT, false, 809, "하위 중분류나 참조 데이터가 있어 삭제할 수 없습니다."),
    CATEGORY_MEDIUM_IN_USE(HttpStatus.CONFLICT, false, 809, "참조 데이터가 있어 삭제할 수 없습니다."),
    CATEGORY_MAJOR_HAS_CHILDREN(HttpStatus.CONFLICT, false, 809, "하위 중분류가 존재하여 삭제할 수 없습니다."),

    /**
     * 900: 문의 Error
     */
    INQUIRY_NO_CONTENT(HttpStatus.BAD_REQUEST, false, 900, "문의 내용이 비어있습니다."),
    INQUIRY_BAD_REQUEST(HttpStatus.BAD_REQUEST, false, 900, "문의 요청이 올바르지 않습니다."),
    INQUIRY_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, false, 901, "문의 작성자만 접근할 수 있습니다."),
    INQUIRY_ANSWER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, false, 901, "문의 답변은 판매자만 등록할 수 있습니다."),
    INQUIRY_CONFLICT(HttpStatus.CONFLICT, false, 902, "문의 처리 중 충돌이 발생했습니다."),
    INQUIRY_ANSWER_CONFLICT(HttpStatus.CONFLICT, false, 902, "이미 답변이 등록된 문의입니다."),
    INQUIRY_ANSWER_FORBIDDEN(HttpStatus.FORBIDDEN, false, 903, "문의 답변에 대한 권한이 없습니다."),
    INQUIRY_FORBIDDEN(HttpStatus.FORBIDDEN, false, 903, "문의에 대한 권한이 없습니다."),
    INQUIRY_CREATE_FORBIDDEN(HttpStatus.FORBIDDEN, false, 903, "판매자는 본인 경매에 문의할 수 없습니다."),
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, false, 904, "문의가 존재하지 않습니다."),
    INQUIRY_ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, false, 904, "문의 답변이 존재하지 않습니다."),
    INQUIRY_AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, false, 904, "존재하지 않는 경매입니다."),
    /**
     * 1000: 미디어 Error
     */
    MEDIA_NOT_FOUND(HttpStatus.NOT_FOUND, false, 1004, "미디어가 존재하지 않습니다."),
    MEDIA_FORBIDDEN(HttpStatus.FORBIDDEN, false, 1003, "미디어에 대한 권한이 없습니다."),
    MEDIA_CONFLICT(HttpStatus.CONFLICT, false, 1002, "미디어 처리 중 충돌이 발생했습니다."),
    MEDIA_NOT_EDITABLE(HttpStatus.CONFLICT, false, 1002, "미디어를 더 이상 수정할 수 없습니다."),
    MEDIA_UNSUPPORTED_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, false, 1007, "지원하지 않는 미디어 형식입니다."),
    MEDIA_FILE_RULE_VIOLATION(HttpStatus.BAD_REQUEST, false, 1008, "미디어 파일이 규격에 맞지 않습니다."),

    /**
     * 1100 : 경매 Error
     */
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, false, 1100, "경매가 존재하지 않습니다."),
    AUCTION_FORBIDDEN(HttpStatus.FORBIDDEN, false, 1101, "경매에 대한 권한이 없습니다."),
    AUCTION_ALREADY_ENDED(HttpStatus.BAD_REQUEST, false, 1102, "이미 종료된 경매입니다."),
    AUCTION_CONFLICT(HttpStatus.CONFLICT, false, 1103, "경매 처리 중 충돌이 발생했습니다."),
    AUCTION_NOT_ENDED(HttpStatus.BAD_REQUEST, false, 1104, "아직 종료되지 않은 경매입니다."),
    AUCTION_BAD_REQUEST(HttpStatus.BAD_REQUEST, false, 1105, "경매 요청이 올바르지 않습니다."),
    AUCTION_CARD_NOT_FOUND(HttpStatus.NOT_FOUND, false, 1106, "카드 정보를 찾을 수 없습니다."),
    AUCTION_BID_FORBIDDEN(HttpStatus.FORBIDDEN, false, 1107, "본인의 경매에는 입찰할 수 없습니다."),
    AUCTION_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, false, 1108, "경매 작성자만 접근할 수 있습니다."),
    AUCTION_GRADE_NOT_FOUND(HttpStatus.NOT_FOUND, false, 1109, "등급 정보(hash)를 찾을 수 없습니다."),
    AUCTION_NO_MEDIA(HttpStatus.BAD_REQUEST, false, 1110, "경매 이미지는 최소 1장 이상 등록해야 합니다."),
    AUCTION_BID_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, false, 1111, "로그인한 회원만 입찰할 수 있습니다."),
    AUCTION_NOT_ACTIVE(HttpStatus.BAD_REQUEST, false, 1112, "종료되었거나 아직 시작하지 않은 경매입니다."),
    AUCTION_CATEGORY_MAJOR_NOT_FOUND(HttpStatus.NOT_FOUND, false, 1113, "카테고리 대분류를 찾을 수 없습니다."),
    AUCTION_DATE_INVALID(HttpStatus.BAD_REQUEST, false, 1114, "경매 종료 시간은 시작 시간보다 이후여야 합니다."),
    AUCTION_CATEGORY_MEDIUM_NOT_FOUND(HttpStatus.NOT_FOUND, false, 1115, "카테고리 중분류를 찾을 수 없습니다."),
    AUCTION_IMPOSSIBLE_TO_EDIT(HttpStatus.BAD_REQUEST, false, 1116, "경매가 시작된 이후에는 수정할 수 없습니다."),
    AUCTION_IMPOSSIBLE_TO_DELETE(HttpStatus.BAD_REQUEST, false, 1117, "경매가 시작된 이후에는 삭제할 수 없습니다."),
    AUCTION_BID_LESS_THAN_CURRENT(HttpStatus.BAD_REQUEST, false, 1118, "현재가보다 높은 금액으로 입찰해야 합니다."),
    AUCTION_BID_INCREASE_LESS_THAN_UNIT(HttpStatus.BAD_REQUEST, false, 1119, "입찰 단위 이상으로 입찰해야 합니다."),

    /**
     * 1200: 카드 Error
     */
    CARD_RARITY_UNSUPPORTED(HttpStatus.BAD_REQUEST, false, 1200, "지원하지 않는 유형의 카드 희귀도입니다."),
    CARD_ATTRIBUTE_UNSUPPORTED(HttpStatus.BAD_REQUEST, false, 1201, "지원하지 않는 유형의 카드 속성입니다."),
    CARD_NAME_DUPLICATED(HttpStatus.CONFLICT, false, 1202, "중복된 카드 이름은 등록할 수 없습니다.");

    private final HttpStatusCode httpStatusCode;
    private final boolean isSuccess;
    private final int code;
    private final String message;

}