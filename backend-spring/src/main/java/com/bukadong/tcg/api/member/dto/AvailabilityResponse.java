package com.bukadong.tcg.api.member.dto;

/**
 * 중복 확인 결과 응답 DTO.
 *
 * <p>
 * 이메일 또는 닉네임의 사용 가능 여부를 클라이언트에 전달한다.
 * </p>
 *
 * <ul>
 * <li>{@code field} - 검증 대상 필드 ("email" 또는 "nickname")</li>
 * <li>{@code value} - 클라이언트가 검증 요청한 값</li>
 * <li>{@code available} - 사용 가능 여부 (true = 사용 가능, false = 이미 존재)</li>
 * </ul>
 *
 * <p>
 * 예시 응답:
 * </p>
 * 
 * <pre>{@code
 * {
 *   "field": "email",
 *   "value": "user@example.com",
 *   "available": true
 * }
 * }</pre>
 */
public record AvailabilityResponse(
        String field,
        String value,
        boolean available) {
}
