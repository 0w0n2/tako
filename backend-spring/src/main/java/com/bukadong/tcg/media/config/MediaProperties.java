package com.bukadong.tcg.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 미디어 업로드 설정 바인딩
 * - application.properties / application.yml 의 "media.*" 키를 바인딩한다.
 * - 하이픈 표기법 → 카멜케이스 자동 매핑: base-dir → baseDir, public-base-url → publicBaseUrl
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "media")
public class MediaProperties {

    public enum Storage {
        LOCAL, S3
    }

    /** media.storage=local|s3 */
    private Storage storage = Storage.LOCAL;

    /** media.allowed-mime-types=... */
    private List<String> allowedMimeTypes;

    /** media.max-size-bytes=... (기본 10MB) */
    private long maxSizeBytes = 10 * 1024 * 1024;

    /** S3 설정 (미사용 시 비워둬도 됨) */
    @Data
    public static class S3 {
        /** media.s3.bucket=... */
        private String bucket;
        /** media.s3.region=ap-northeast-2 */
        private String region;
        /** media.s3.public-base-url=https://cdn.example.com */
        private String publicBaseUrl;
        /** media.s3.key-prefix=uploads */
        private String keyPrefix = "uploads";
    }

    /** 로컬 저장소 설정 */
    @Data
    public static class Local {
        /** media.local.base-dir=C:/app/uploads */
        private String baseDir = "C:/app/uploads";
        /** media.local.public-base-url=http://localhost:8080/uploads */
        private String publicBaseUrl = "/uploads";
    }

    private S3 s3 = new S3();
    private Local local = new Local();
}
