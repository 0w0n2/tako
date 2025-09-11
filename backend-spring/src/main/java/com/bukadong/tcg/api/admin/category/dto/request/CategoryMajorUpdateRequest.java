package com.bukadong.tcg.api.admin.category.dto.request;

import com.bukadong.tcg.global.constant.Patterns;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = Patterns.NO_WHITESPACE, message = "대분류명은 공백만으로 이루어질 수 없습니다.")
    @Schema(description = "대분류명", example = "포켓몬 카드(수정)")
    @Size(max = 30)
    private String name;

    @Pattern(regexp = Patterns.NO_WHITESPACE, message = "설명은 공백만으로 이루어질 수 없습니다.")
    @Schema(description = "대분류 설명", example = "포켓몬 관련 카드 대분류(수정)")
    @Size(max = 255)
    private String description;
}
