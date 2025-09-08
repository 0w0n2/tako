package com.bukadong.tcg.category.service;

import com.bukadong.tcg.category.entity.CategoryMajor;
import com.bukadong.tcg.category.entity.CategoryMedium;
import com.bukadong.tcg.category.repository.CategoryMajorRepository;
import com.bukadong.tcg.category.repository.CategoryMediumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 카테고리 조회 서비스.
 *
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
    public List<CategoryMajor> listMajors() {
        // 필요 시 정렬이 있다면 Sort.by("...")로 정렬 조건을 추가.
        return categoryMajorRepository.findAll();
    }

    /**
     * 특정 대분류에 속한 중분류 목록을 조회한다.
     *
     * @param majorId 대분류 ID (null 또는 0 이하인 경우 예외 처리 고려 가능)
     * @return 중분류 목록
     */
    public List<CategoryMedium> listMediumsByMajorId(Long majorId) {
        // 중요 로직 주석: 기본적으로 연관 키로 전체 조회만 수행
        // (엔티티 매핑이 major(@ManyToOne)인 경우 findByMajor_Id 사용)
        return categoryMediumRepository.findByCategoryMajor_Id(majorId);
    }
}
