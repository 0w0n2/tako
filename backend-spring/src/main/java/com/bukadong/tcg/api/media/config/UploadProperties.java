package com.bukadong.tcg.api.media.config;

import com.bukadong.tcg.api.media.entity.MediaType;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    /** 타입별 업로드 제한 규칙 (YAML의 app.upload.limits) */
    private Map<MediaType, Limit> limits = new EnumMap<>(MediaType.class);

    @Data
    public static class Limit {
        /** 개별 파일 최대 크기 */
        private DataSize perFile;
        /** 요청 단위 총합 최대 크기 */
        private DataSize perRequest;
        /** 요청당 최대 파일 개수 (null/<=0이면 무제한) */
        private Integer maxCount;

        /** 허용되는 MIME 목록 (정확일치 또는 접두 허용: "image/*", "image/") */
        private List<String> allowedMimes;
        /** 허용 확장자 (소문자, 점 제외: jpg, png 등) */
        private List<String> allowedExts;

        /** 이미지 전용 규격 제한 */
        private Image image;
    }

    @Data
    public static class Image {
        private Boolean square; // true면 정사각형만
        private Integer minWidth;
        private Integer minHeight;
        private Integer maxWidth;
        private Integer maxHeight;
    }
}
