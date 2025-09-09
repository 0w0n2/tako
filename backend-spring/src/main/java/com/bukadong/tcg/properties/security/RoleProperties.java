package com.bukadong.tcg.properties.security;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Spring Security 역할(Role) 기반의 경로 접근 제어 설정을 담는 클래스
 *
 * @param admin 'ADMIN' 역할을 가진 사용자만 접근할 수 있는 URL 경로 패턴 목록
 */
@ConfigurationProperties(prefix = "security.role")
public record RoleProperties(
        List<String> admin
) {
}
