package com.bukadong.tcg.api.category.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 대분류 카테고리 응답 DTO
 * <P>
 * 엔티티를 직접 노출하지 않고 필요한 정보만 반환한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Schema(description = "대분류 카테고리 응답")
public record CategoryMajorResponse(@Schema(description = "대분류 ID", example = "1001") Long id,
        @Schema(description = "대분류 이름", example = "TCG") String name,
        @Schema(description = "설명", example = "트레이딩 카드 게임") String description,
        @Schema(description = "대표 이미지 URL(프리사인, 5분 유효)", example = "https://s3.example.com/...") String imageUrl) {
}
