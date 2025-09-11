package com.bukadong.tcg.api.admin.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 대분류 생성 요청 DTO
 * <P>
 * 이름/설명을 받아 대분류를 생성합니다.
 * </P>
 * 
 * @PARAM name, description
 * @RETURN 없음
 */
@Getter
@Setter
public class CategoryMajorCreateRequest {

    @Schema(description = "대분류명", example = "포켓몬 카드")
    @NotBlank
    @Size(max = 30)
    private String name;

    @Schema(description = "대분류 설명", example = "포켓몬 관련 카드 대분류")
    @NotBlank
    @Size(max = 255)
    private String description;
}
