package com.bukadong.tcg.api.admin.category.controller;

import com.bukadong.tcg.api.admin.category.dto.request.CategoryMajorCreateRequest;
import com.bukadong.tcg.api.admin.category.dto.request.CategoryMajorUpdateRequest;
import com.bukadong.tcg.api.admin.category.dto.response.CategoryMajorResponse;
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
 * 카테고리 대분류 관리자 컨트롤러
 * <P>
 * 대분류 생성/수정/삭제를 제공합니다. ROLE_ADMIN만 접근 가능합니다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN BaseResponse
 */
@Tag(name = "Admin - Categories (Major)", description = "카테고리 대분류 관리자 API")
@RestController
@RequestMapping("/v1/admin/categories/majors")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminCategoryMajorController {

    private final AdminCategoryCommandService adminCategoryCommandService;

    /**
     * 대분류 생성
     * <P>
     * 이름 중복, 입력 검증은 서비스에서 수행합니다.
     * </P>
     * 
     * @PARAM request 생성 요청 DTO
     * @RETURN BaseResponse<CategoryMajorResponse>
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "대분류 생성", description = "새로운 카테고리 대분류를 생성합니다. (ADMIN)")
    @PostMapping
    public BaseResponse<CategoryMajorResponse> create(@Valid @RequestBody CategoryMajorCreateRequest request) {
        return BaseResponse.onSuccess(adminCategoryCommandService.createMajor(request));
    }

    /**
     * 대분류 수정
     * <P>
     * 존재하지 않으면 NOT_FOUND 예외를 발생시킵니다.
     * </P>
     * 
     * @PARAM majorId 대분류 ID
     * @PARAM request 수정 요청 DTO
     * @RETURN BaseResponse<CategoryMajorResponse>
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "대분류 수정", description = "카테고리 대분류 정보를 수정합니다. (ADMIN)")
    @PutMapping("/{majorId}")
    public BaseResponse<CategoryMajorResponse> update(
            @Parameter(name = "majorId", description = "대분류 ID", required = true) @PathVariable("majorId") @NotNull Long majorId,
            @Valid @RequestBody CategoryMajorUpdateRequest request) {
        return BaseResponse.onSuccess(adminCategoryCommandService.updateMajor(majorId, request));
    }

    /**
     * 대분류 삭제
     * <P>
     * 하위 중분류/연관 데이터 존재 시 정책에 따라 차단 또는 소프트 삭제합니다.
     * </P>
     * 
     * @PARAM majorId 대분류 ID
     * @RETURN BaseResponse<Void>
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "대분류 삭제", description = "카테고리 대분류를 삭제합니다. (ADMIN)")
    @DeleteMapping("/{majorId}")
    public BaseResponse<Void> delete(
            @Parameter(name = "majorId", description = "대분류 ID", required = true) @PathVariable("majorId") @NotNull Long majorId) {
        adminCategoryCommandService.deleteMajor(majorId);
        return BaseResponse.onSuccess();
    }
}
