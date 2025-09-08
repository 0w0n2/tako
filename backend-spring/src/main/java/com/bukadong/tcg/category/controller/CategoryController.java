package com.bukadong.tcg.category.controller;

import com.bukadong.tcg.category.entity.CategoryMajor;
import com.bukadong.tcg.category.service.CategoryQueryService;
import com.bukadong.tcg.common.base.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 조회 API (공개)
 *
 * <p>
 * 컨트롤러는 입출력 틀만 담당하고, 로직은 Service에 위임한다.
 * 공통 응답 체계(BaseResponse)를 사용한다.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryQueryService categoryQueryService;

    /**
     * 전체 카테고리 조회
     *
     * @return 모든 카테고리 목록을 감싼 BaseResponse
     */
    @GetMapping
    public BaseResponse<List<CategoryMajor>> list() {
        return new BaseResponse<>(categoryQueryService.listAll());
    }

    /**
     * 코드로 카테고리 단건 조회
     *
     * @param code 카테고리 코드
     * @return 카테고리 단건을 감싼 BaseResponse
     */
    @GetMapping("/{code}")
    public BaseResponse<CategoryMajor> getByCode(@PathVariable String code) {
        return new BaseResponse<>(categoryQueryService.getByCode(code));
    }
}
