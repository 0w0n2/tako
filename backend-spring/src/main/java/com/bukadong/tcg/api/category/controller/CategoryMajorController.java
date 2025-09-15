package com.bukadong.tcg.api.category.controller;

import com.bukadong.tcg.api.category.dto.response.CategoryMajorResponse;
import com.bukadong.tcg.api.category.entity.CategoryMajor;
import com.bukadong.tcg.api.category.service.CategoryQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 대분류 조회 API (공개).
 * <p>
 * 입출력 틀만 담당하고, 조회 로직은 {@link CategoryQueryService}에 위임한다. 공통 응답 체계
 * {@link BaseResponse}를 사용한다.
 * </p>
 */
@RestController
@RequestMapping("/v1/categories/majors")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "카테고리 대분류 조회 API")
public class CategoryMajorController {

    private final CategoryQueryService categoryQueryService;

    /**
     * 전체 대분류 조회.
     *
     * @return 대분류 목록을 감싼 BaseResponse
     */
    @Operation(summary = "대분류 전체 조회", description = "등록된 모든 카테고리 대분류 목록을 반환합니다.")
    @GetMapping
    public BaseResponse<List<CategoryMajorResponse>> listMajors() {
        return BaseResponse.onSuccess(categoryQueryService.listMajors());
    }
}
