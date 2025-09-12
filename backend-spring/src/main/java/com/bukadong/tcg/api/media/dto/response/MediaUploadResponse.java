package com.bukadong.tcg.api.media.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 미디어 업로드 응답
 * <P>
 * 단건/다건 업로드 모두 대응.
 * </P>
 * 
 * @PARAM items 업로드 항목 리스트
 * @RETURN 없음
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaUploadResponse {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        @Schema(description = "S3 오브젝트 키")
        private String key;
        @Schema(description = "원본 파일명")
        private String originalFilename;
        @Schema(description = "MIME 타입")
        private String contentType;
        @Schema(description = "사이즈(byte)")
        private long size;
    }

    @Schema(description = "업로드 결과 목록")
    private List<Item> items;
}
