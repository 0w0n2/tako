package com.bukadong.tcg.category.repository;

import com.bukadong.tcg.category.entity.CategoryMajor;
import com.bukadong.tcg.category.entity.CategoryMedium;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 중분류({@link CategoryMedium}) 전용 레포지토리.
 *
 * <p>
 * (대분류, 이름) 복합 유니크 제약을 활용하는 조회 메서드를 제공한다.
 * </p>
 */
public interface CategoryMediumRepository extends JpaRepository<CategoryMedium, Long> {

    /**
     * 특정 대분류에 속한 모든 중분류를 조회한다.
     *
     * @param major 대분류 엔티티
     * @return 중분류 목록
     */
    List<CategoryMedium> findByCategoryMajor(CategoryMajor major);

    /**
     * (대분류, 이름) 조합으로 중분류를 단건 조회한다.
     *
     * @param major 대분류 엔티티
     * @param name  중분류 이름
     * @return 존재 시 해당 엔티티, 없으면 빈 Optional
     */
    Optional<CategoryMedium> findByCategoryMajorAndName(CategoryMajor major, String name);
}
