package com.bukadong.tcg.api.admin.category.controller;

import com.bukadong.tcg.api.admin.category.dto.request.CategoryMediumCreateRequest;
import com.bukadong.tcg.api.admin.category.dto.request.CategoryMediumUpdateRequest;
import com.bukadong.tcg.api.admin.category.dto.response.CategoryMediumResponse;
import com.bukadong.tcg.api.admin.category.service.AdminCategoryCommandService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 카테고리 중분류 관리자 컨트롤러
 * <P>
 * 중분류 생성/수정/삭제를 제공합니다. ROLE_ADMIN만 접근 가능합니다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN BaseResponse
 */
@Tag(name = "Admin - Categories (Medium)", description = "카테고리 중분류 관리자 API")
@RestController
@RequestMapping("/v1/admin/categories/mediums")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminCategoryMediumController {

    private final AdminCategoryCommandService adminCategoryCommandService;

    /**
     * 중분류 생성
     * <P>
     * 상위 대분류 존재 여부 및 이름 중복을 검증합니다.
     * </P>
     * 
     * @PARAM request 생성 요청 DTO
     * @RETURN BaseResponse<CategoryMediumResponse>
     */
    @Operation(summary = "중분류 생성", description = "대상 대분류 하위에 중분류를 생성합니다. (ADMIN)")
    @PostMapping
    public BaseResponse<CategoryMediumResponse> create(@Valid @RequestBody CategoryMediumCreateRequest request) {
        return BaseResponse.onSuccess(adminCategoryCommandService.createMedium(request));
    }

    /**
     * 중분류 수정
     * <P>
     * 대상/상위가 없으면 NOT_FOUND를 반환합니다.
     * </P>
     * 
     * @PARAM mediumId 중분류 ID
     * @PARAM request 수정 요청 DTO
     * @RETURN BaseResponse<CategoryMediumResponse>
     */
    @Operation(summary = "중분류 수정", description = "카테고리 중분류 정보를 수정합니다. (ADMIN)")
    @PutMapping("/{mediumId}")
    public BaseResponse<CategoryMediumResponse> update(
            @Parameter(name = "mediumId", description = "중분류 ID", required = true) @PathVariable("mediumId") @NotNull Long mediumId,
            @Valid @RequestBody CategoryMediumUpdateRequest request) {
        return BaseResponse.onSuccess(adminCategoryCommandService.updateMedium(mediumId, request));
    }

    /**
     * 중분류 삭제
     * <P>
     * 연관 데이터가 있으면 정책에 따라 차단 또는 소프트 삭제합니다.
     * </P>
     * 
     * @PARAM mediumId 중분류 ID
     * @RETURN BaseResponse<Void>
     */
    @Operation(summary = "중분류 삭제", description = "카테고리 중분류를 삭제합니다. (ADMIN)")
    @DeleteMapping("/{mediumId}")
    public BaseResponse<Void> delete(
            @Parameter(name = "mediumId", description = "중분류 ID", required = true) @PathVariable("mediumId") @NotNull Long mediumId) {
        adminCategoryCommandService.deleteMedium(mediumId);
        return BaseResponse.onSuccess();
    }
}
