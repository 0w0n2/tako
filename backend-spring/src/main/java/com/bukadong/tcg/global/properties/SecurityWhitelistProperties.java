package com.bukadong.tcg.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring Security 에서 인증을 무시할 경로(Whitelist) 목록을 관리하는 설정 클래스
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "security.whitelist")
public class SecurityWhitelistProperties {

    private Map<String, List<String>> whitelist;

    private Map<HttpMethod, List<String>> parsedWhitelist;

    public Map<HttpMethod, List<String>> getParsedWhitelist() {
        if (parsedWhitelist == null) {
            Map<HttpMethod, List<String>> parsed = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : whitelist.entrySet()) {
                HttpMethod httpMethod = HttpMethod.valueOf(entry.getKey().toUpperCase());
                parsed.put(httpMethod, entry.getValue());
            }
            this.parsedWhitelist = parsed;
        }
        return this.parsedWhitelist;
    }
}
