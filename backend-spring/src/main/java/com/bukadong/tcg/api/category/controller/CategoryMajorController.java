package com.bukadong.tcg.api.category.controller;

import com.bukadong.tcg.api.category.entity.CategoryMajor;
import com.bukadong.tcg.api.category.service.CategoryQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
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
public class CategoryMajorController {

    private final CategoryQueryService categoryQueryService;

    /**
     * 전체 대분류 조회.
     *
     * @return 대분류 목록을 감싼 BaseResponse
     */
    @GetMapping
    public BaseResponse<List<CategoryMajor>> listMajors() {
        return new BaseResponse<>(categoryQueryService.listMajors());
    }
}
