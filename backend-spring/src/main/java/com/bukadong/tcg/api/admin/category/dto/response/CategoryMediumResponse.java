package com.bukadong.tcg.api.admin.category.dto.response;

import com.bukadong.tcg.api.category.entity.CategoryMedium;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 중분류 응답 DTO
 * <P>
 * 엔티티를 API 응답 형태로 변환합니다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Getter
@AllArgsConstructor
public class CategoryMediumResponse {

    @Schema(description = "중분류 ID", example = "10000")
    private Long id;

    @Schema(description = "상위 대분류 ID", example = "1000")
    private Long majorId;

    @Schema(description = "중분류명", example = "1세대")
    private String name;

    @Schema(description = "중분류 설명", example = "포켓몬 1세대 카드")
    private String description;

    public static CategoryMediumResponse from(CategoryMedium medium) {
        return new CategoryMediumResponse(medium.getId(), medium.getCategoryMajor().getId(), medium.getName(),
                medium.getDescription());
    }
}
