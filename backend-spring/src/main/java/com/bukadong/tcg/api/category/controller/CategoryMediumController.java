package com.bukadong.tcg.api.category.controller;

import com.bukadong.tcg.api.category.dto.response.CategoryMediumResponse;
import com.bukadong.tcg.api.category.entity.CategoryMedium;
import com.bukadong.tcg.api.category.service.CategoryQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 중분류 조회 API (공개).
 * <p>
 * 입출력 틀만 담당하고, 조회 로직은 {@link CategoryQueryService}에 위임한다. 공통 응답 체계
 * {@link BaseResponse}를 사용한다.
 * </p>
 */
@RestController
@RequestMapping("/v1/categories/mediums")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "카테고리 중분류 조회 API")
public class CategoryMediumController {

    private final CategoryQueryService categoryQueryService;

    /**
     * 특정 대분류에 속한 중분류 목록 조회.
     *
     * @param majorId 대분류 ID
     * @return 중분류 목록을 감싼 BaseResponse
     */
    @Operation(summary = "중분류 목록 조회", description = "특정 대분류(majorId)에 속한 중분류 목록을 반환합니다.")
    @GetMapping("/{majorId}")
    public BaseResponse<List<CategoryMediumResponse>> listMediumsByMajor(
            @Parameter(description = "대분류 ID") @PathVariable("majorId") Long majorId) {
        return BaseResponse.onSuccess(categoryQueryService.listMediumsByMajorId(majorId));
    }
}
