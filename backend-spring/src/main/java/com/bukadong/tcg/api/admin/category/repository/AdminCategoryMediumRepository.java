package com.bukadong.tcg.api.admin.category.repository;

import com.bukadong.tcg.api.category.entity.CategoryMedium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 전용 중분류 리포지토리
 * <P>
 * 쓰기/일괄 삭제 등 관리 작업을 제공합니다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN JpaRepository
 */
@Repository
public interface AdminCategoryMediumRepository extends JpaRepository<CategoryMedium, Long> {
    boolean existsByCategoryMajor_Id(Long categoryMajorId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteAllByCategoryMajorId(Long categoryMajorId);

    boolean existsByCategoryMajor_IdAndName(Long categoryMajorId, String name);

    boolean existsByCategoryMajor_IdAndNameAndIdNot(Long categoryMajorId, String name, Long id);
}
