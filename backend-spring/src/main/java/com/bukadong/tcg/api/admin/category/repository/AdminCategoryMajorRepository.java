package com.bukadong.tcg.api.admin.category.repository;

import com.bukadong.tcg.api.category.entity.CategoryMajor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 관리자 전용 대분류 리포지토리
 * <P>
 * 중복 체크/쓰기 전용 확장을 위해 분리했습니다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN JpaRepository
 */
@Repository
public interface AdminCategoryMajorRepository extends JpaRepository<CategoryMajor, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
