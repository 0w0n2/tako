package com.bukadong.tcg.category.repository;

import com.bukadong.tcg.category.entity.CategoryMajor;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 카테고리 대분류({@link CategoryMajor}) 전용 레포지토리.
 *
 * <p>
 * 기본 CRUD 외에 이름 기반 단건 조회를 제공한다.
 * </p>
 */
public interface CategoryMajorRepository extends JpaRepository<CategoryMajor, Long> {
}
