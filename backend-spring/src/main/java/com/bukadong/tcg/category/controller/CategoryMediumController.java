package com.bukadong.tcg.category.controller;

import com.bukadong.tcg.category.entity.CategoryMedium;
import com.bukadong.tcg.category.service.CategoryQueryService;
import com.bukadong.tcg.common.base.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 중분류 조회 API (공개).
 *
 * <p>
 * 입출력 틀만 담당하고, 조회 로직은 {@link CategoryQueryService}에 위임한다.
 * 공통 응답 체계 {@link BaseResponse}를 사용한다.
 * </p>
 */
@RestController
@RequestMapping("/v1/categories/mediums")
@RequiredArgsConstructor
public class CategoryMediumController {

    private final CategoryQueryService categoryQueryService;

    /**
     * 특정 대분류에 속한 중분류 목록 조회.
     *
     * @param majorId 대분류 ID
     * @return 중분류 목록을 감싼 BaseResponse
     */
    @GetMapping("/{majorId}")
    public BaseResponse<List<CategoryMedium>> listMediumsByMajor(@PathVariable Long majorId) {
        return new BaseResponse<>(categoryQueryService.listMediumsByMajorId(majorId));
    }
}
