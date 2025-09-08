package com.bukadong.tcg.category.service;

import com.bukadong.tcg.category.entity.CategoryMajor;
import com.bukadong.tcg.category.entity.CategoryMedium;
import com.bukadong.tcg.category.repository.CategoryMajorRepository;
import com.bukadong.tcg.category.repository.CategoryMediumRepository;
import com.bukadong.tcg.common.base.BaseResponseStatus;
import com.bukadong.tcg.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 카테고리 읽기 전용 조회 서비스.
 *
 * <p>
 * 대분류/중분류 조회 로직을 캡슐화한다.
 * 공통 예외 체계(BaseException, BaseResponseStatus)를 사용한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryQueryService {

    private final CategoryMajorRepository categoryMajorRepository;
    private final CategoryMediumRepository categoryMediumRepository;

    // =========================
    // 대분류 (CategoryMajor)
    // =========================

    /**
     * 모든 대분류를 조회한다.
     *
     * @return 대분류 목록
     */
    public List<CategoryMajor> listMajors() {
        return categoryMajorRepository.findAll();
    }

    /**
     * 대분류를 이름으로 조회한다.
     *
     * @param name 대분류 이름
     * @return 대분류
     * @throws BaseException BAD_REQUEST(파라미터 누락), NOT_FOUND(없음)
     */
    public CategoryMajor getMajorByName(String name) {
        if (name == null || name.isBlank()) {
            throw new BaseException(BaseResponseStatus.CATEGORY_BAD_REQUEST);
        }
        return categoryMajorRepository.findByName(name)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));
    }

    /**
     * 대분류를 ID로 조회한다.
     *
     * @param id 대분류 ID
     * @return 대분류
     * @throws BaseException NOT_FOUND
     */
    public CategoryMajor getMajorById(Long id) {
        return categoryMajorRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
    }

    // =========================
    // 중분류 (CategoryMedium)
    // =========================

    /**
     * 특정 대분류에 속한 중분류 목록을 조회한다.
     *
     * @param majorId 대분류 ID
     * @return 중분류 목록
     * @throws BaseException NOT_FOUND(대분류 없음)
     */
    public List<CategoryMedium> listMediumsByMajorId(Long majorId) {
        CategoryMajor major = getMajorById(majorId);
        return categoryMediumRepository.findByCategoryMajor(major);
    }

    /**
     * (대분류ID, 중분류명)으로 중분류 단건을 조회한다.
     *
     * @param majorId    대분류 ID
     * @param mediumName 중분류 이름
     * @return 중분류
     * @throws BaseException BAD_REQUEST(파라미터 누락), NOT_FOUND(없음)
     */
    public CategoryMedium getMediumByMajorAndName(Long majorId, String mediumName) {
        if (mediumName == null || mediumName.isBlank()) {
            throw new BaseException(BaseResponseStatus.CATEGORY_BAD_REQUEST);
        }
        CategoryMajor major = getMajorById(majorId);
        return categoryMediumRepository.findByCategoryMajorAndName(major, mediumName)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));
    }

    /**
     * 중분류를 ID로 조회한다.
     *
     * @param mediumId 중분류 ID
     * @return 중분류
     * @throws BaseException NOT_FOUND
     */
    public CategoryMedium getMediumById(Long mediumId) {
        return categoryMediumRepository.findById(mediumId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));
    }
}
