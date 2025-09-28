package com.bukadong.tcg.api.admin.category.dto.request;

import com.bukadong.tcg.global.constant.Patterns;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 중분류 수정 요청 DTO
 * <P>
 * 부분 수정 허용(Null 필드는 미변경). 상위 대분류 변경도 지원합니다.
 * </P>
 * 
 * @PARAM majorId, name, description
 * @RETURN 없음
 */
@Getter
@Setter
public class CategoryMediumUpdateRequest {

    @Schema(description = "변경할 상위 대분류 ID", example = "1001")
    private Long majorId;

    @Pattern(regexp = Patterns.NO_WHITESPACE, message = "중분류명은 공백만으로 이루어질 수 없습니다.")
    @Schema(description = "중분류명", example = "1세대(수정)")
    @Size(max = 30)
    private String name;

    @Pattern(regexp = Patterns.NO_WHITESPACE, message = "설명은 공백만으로 이루어질 수 없습니다.")
    @Schema(description = "중분류 설명", example = "포켓몬 1세대 카드(수정)")
    @Size(max = 255)
    private String description;
}
