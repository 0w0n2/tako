package com.bukadong.tcg.api.admin.category.dto.response;

import com.bukadong.tcg.api.category.entity.CategoryMajor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 대분류 응답 DTO
 * <P>
 * 엔티티를 API 응답 형태로 변환합니다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Getter
@AllArgsConstructor
public class CategoryMajorResponse {

    @Schema(description = "대분류 ID", example = "1000")
    private Long id;

    @Schema(description = "대분류명", example = "포켓몬 카드")
    private String name;

    @Schema(description = "대분류 설명", example = "포켓몬 관련 카드 대분류")
    private String description;

    public static CategoryMajorResponse from(CategoryMajor major) {
        return new CategoryMajorResponse(major.getId(), major.getName(), major.getDescription());
    }
}
