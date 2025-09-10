package com.bukadong.tcg.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Spring Security CORS 관련 설정을 담는 클래스
 *
 * @param allowedOrigins   허용할 Origin 목록
 * @param allowedMethods   허용할 HTTP 메서드 목록
 * @param allowedHeaders   클라이언트가 사용하도록 허용된 헤더 목록
 * @param allowCredentials 서버 응답에서 노출할 헤더 목록
 * @param exposedHeaders   자격 증명(쿠키, HTTP 인증 등)을 포함한 요청을 허용할지 여부
 * @param maxAge           브라우저가 pre-flight 요청의 응답을 캐시할 시간(초)을 설정
 */
@ConfigurationProperties(prefix = "security.cors")
public record SecurityCorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        Boolean allowCredentials,
        List<String> exposedHeaders,
        Long maxAge
) {
}
