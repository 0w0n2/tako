package com.bukadong.tcg.api.fcm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Firebase 초기화 설정.
 * <p>
 * application.yml 의 fcm.file_path 로 주입된 서비스 계정 키(JSON) 파일을 이용해 FirebaseApp 단일
 * 인스턴스를 초기화한다. 로컬/서버 모두 동일하게 동작. 키 파일은 Git 에 커밋하지 말고 환경변수 또는 외부 경로 마운트로 주입.
 * </p>
 */
@Configuration
@EnableConfigurationProperties(PushProperties.class)
@Slf4j
public class FirebaseConfig {

    @Value("${fcm.service-account.base64:}")
    private String serviceAccountBase64;

    // 기본 생성자 (Spring이 프록시/빈 생성 시 사용).
    // 필드 주입(@Value)만 필요하고 추가 초기화 로직이 없으므로 비어있음.
    public FirebaseConfig() {
        /* no-op */
    }

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        if (serviceAccountBase64 == null || serviceAccountBase64.isBlank()) {
            throw new IllegalStateException(
                    "FIREBASE_SERVICE_ACCOUNT_B64 (fcm.service-account.base64) 가 비어있습니다. Base64 only 모드에서 필수입니다.");
        }

        log.info("[FCM] Using Base64 encoded service account (length={})", serviceAccountBase64.length());

        try (InputStream is = decodeBase64Json()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(is);
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("[FCM] Initialized FirebaseApp (authType={})", credentials.getAuthenticationType());
            return app;
        } catch (Exception e) {
            log.error("[FCM] Failed to initialize FirebaseApp (Base64 mode)", e);
            throw e;
        }
    }

    private InputStream decodeBase64Json() {
        String compact = serviceAccountBase64.replace("\r", "").replace("\n", "").trim();
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(compact);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid Base64 for fcm.service-account.base64", e);
        }
        String json = new String(decoded, StandardCharsets.UTF_8);
        if (!json.trim().startsWith("{")) {
            throw new IllegalStateException("Decoded credential is not valid JSON");
        }
        // 민감 정보 전체를 로그로 남기지 않고 핵심 식별자(project_id, client_email 일부)만 마스킹 출력
        try {
            String projectId = extractJsonValue(json, "project_id");
            String clientEmail = extractJsonValue(json, "client_email");
            if (clientEmail != null && clientEmail.length() > 5) {
                clientEmail = clientEmail.substring(0, Math.min(5, clientEmail.length())) + "***";
            }
            log.info("[FCM] Decoded service account summary project_id='{}', client_email='{}'", projectId,
                    clientEmail);
        } catch (Exception ex) {
            log.debug("[FCM] Failed to extract summary fields from service account JSON: {}", ex.getMessage());
        }
        return new ByteArrayInputStream(decoded);
    }

    private String extractJsonValue(String json, String key) {
        // 매우 단순한 키 추출 (정식 파서 사용 대신 종속성 최소화). 키가 없으면 null 반환.
        // "key": "value" 형태 검색.
        String pattern = "\"" + key + "\""; // "key"
        int idx = json.indexOf(pattern);
        if (idx < 0)
            return null;
        int colon = json.indexOf(':', idx + pattern.length());
        if (colon < 0)
            return null;
        int firstQuote = json.indexOf('"', colon + 1);
        if (firstQuote < 0)
            return null;
        int secondQuote = json.indexOf('"', firstQuote + 1);
        if (secondQuote < 0)
            return null;
        return json.substring(firstQuote + 1, secondQuote);
    }
}
