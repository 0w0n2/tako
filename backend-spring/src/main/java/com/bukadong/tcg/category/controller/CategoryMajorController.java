package com.bukadong.tcg.category.controller;

import com.bukadong.tcg.category.entity.CategoryMajor;
import com.bukadong.tcg.category.service.CategoryQueryService;
import com.bukadong.tcg.common.base.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 대분류 조회 API (공개).
 *
 * <p>
 * 입출력 틀만 담당하고, 조회 로직은 {@link CategoryQueryService}에 위임한다.
 * 공통 응답 체계 {@link BaseResponse}를 사용한다.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/categories/majors")
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

    /**
     * 대분류 단건 조회 (ID 기준).
     *
     * @param id 대분류 ID
     * @return 대분류 단건을 감싼 BaseResponse
     */
    @GetMapping("/{id}")
    public BaseResponse<CategoryMajor> getMajorById(@PathVariable Long id) {
        return new BaseResponse<>(categoryQueryService.getMajorById(id));
    }

    /**
     * 대분류 단건 조회 (이름 기준).
     *
     * @param name 대분류 이름
     * @return 대분류 단건을 감싼 BaseResponse
     */
    @GetMapping("/name/{name}")
    public BaseResponse<CategoryMajor> getMajorByName(@PathVariable String name) {
        return new BaseResponse<>(categoryQueryService.getMajorByName(name));
    }
}
