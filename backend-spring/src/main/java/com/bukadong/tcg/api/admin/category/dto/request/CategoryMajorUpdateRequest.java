package com.bukadong.tcg.api.admin.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 대분류 수정 요청 DTO
 * <P>
 * 부분 수정 허용(Null 필드는 미변경).
 * </P>
 * 
 * @PARAM name, description
 * @RETURN 없음
 */
@Getter
@Setter
public class CategoryMajorUpdateRequest {

    @Schema(description = "대분류명", example = "포켓몬 카드(수정)")
    @Size(max = 30)
    private String name;

    @Schema(description = "대분류 설명", example = "포켓몬 관련 카드 대분류(수정)")
    @Size(max = 255)
    private String description;
}
