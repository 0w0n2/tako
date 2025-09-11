package com.bukadong.tcg.api.admin.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "중분류명", example = "1세대(수정)")
    @Size(max = 30)
    private String name;

    @Schema(description = "중분류 설명", example = "포켓몬 1세대 카드(수정)")
    @Size(max = 255)
    private String description;
}
