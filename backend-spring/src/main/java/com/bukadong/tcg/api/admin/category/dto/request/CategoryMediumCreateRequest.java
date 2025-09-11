package com.bukadong.tcg.api.admin.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 중분류 생성 요청 DTO
 * <P>
 * 상위 대분류 ID와 이름/설명을 받아 생성합니다.
 * </P>
 * 
 * @PARAM majorId, name, description
 * @RETURN 없음
 */
@Getter
@Setter
public class CategoryMediumCreateRequest {

    @Schema(description = "상위 대분류 ID", example = "1000")
    @NotNull(message = "상위 대분류 ID는 필수입니다.")
    private Long majorId;

    @Schema(description = "중분류명", example = "1세대")
    @NotBlank(message = "중분류명은 필수입니다.")
    @Size(max = 30)
    private String name;

    @Schema(description = "중분류 설명", example = "포켓몬 1세대 카드")
    @NotBlank(message = "설명은 필수입니다.")
    @Size(max = 255)
    private String description;
}
