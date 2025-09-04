package com.bukadong.tcg.category.controller;

import com.bukadong.tcg.category.entity.Category;
import com.bukadong.tcg.category.repository.CategoryRepository;
import com.bukadong.tcg.common.base.BaseResponse;
import com.bukadong.tcg.common.base.BaseResponseStatus;
import com.bukadong.tcg.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 조회 API (공개)
 *
 * <p>
 * 프로젝트의 공통 응답/예외 체계(BaseResponse, BaseException, BaseResponseStatus)를 사용한다.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    /**
     * 전체 카테고리 조회
     *
     * @return 모든 카테고리 목록을 감싼 BaseResponse
     */
    @GetMapping
    public BaseResponse<List<Category>> list() {
        List<Category> categories = categoryRepository.findAll();
        return new BaseResponse<>(categories);
    }

    /**
     * 코드로 카테고리 단건 조회
     *
     * @param code 카테고리 코드
     * @return 카테고리 단건을 감싼 BaseResponse
     * @throws BaseException 카테고리를 찾을 수 없는 경우 공통 예외 던짐
     */
    @GetMapping("/{code}")
    public BaseResponse<Category> getByCode(@PathVariable String code) {
        // 중요 로직: 존재하지 않으면 공통 예외(BaseException)로 흐름을 올림
        Category category = categoryRepository.findByCode(code)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
        return new BaseResponse<>(category);
    }
}
