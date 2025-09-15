package com.bukadong.tcg.api.category.service;

import com.bukadong.tcg.api.category.dto.response.CategoryMajorResponse;
import com.bukadong.tcg.api.category.dto.response.CategoryMediumResponse;
import com.bukadong.tcg.api.category.entity.CategoryMajor;
import com.bukadong.tcg.api.category.entity.CategoryMedium;
import com.bukadong.tcg.api.category.repository.CategoryMajorRepository;
import com.bukadong.tcg.api.category.repository.CategoryMediumRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 카테고리 조회 서비스.
 * <p>
 * 대분류 전체 조회, 대분류 ID 기준 중분류 전체 조회를 제공한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryQueryService {

    private final CategoryMajorRepository categoryMajorRepository;
    private final CategoryMediumRepository categoryMediumRepository;

    /**
     * 전체 대분류 목록을 조회한다.
     *
     * @return 대분류 목록
     */
    public List<CategoryMajorResponse> listMajors() {
        return categoryMajorRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(m -> new CategoryMajorResponse(m.getId(), m.getName(), m.getDescription())).toList();
    }

    /**
     * 특정 대분류에 속한 중분류 목록을 조회한다.
     *
     * @param majorId 대분류 ID
     * @return 중분류 응답 DTO 목록
     */
    public List<CategoryMediumResponse> listMediumsByMajorId(Long majorId) {
        return categoryMediumRepository.findByCategoryMajor_Id(majorId).stream()
                .map(m -> new CategoryMediumResponse(m.getId(), m.getName(), m.getDescription(),
                        m.getCategoryMajor().getId(), // 트랜잭션 내 + EntityGraph로 안전
                        m.getCategoryMajor().getName()))
                .toList();
    }
}
