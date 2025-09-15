package com.bukadong.tcg.api.category.repository;

import com.bukadong.tcg.api.category.entity.CategoryMedium;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 중분류 리포지토리.
 * <p>
 * 대분류 ID 기준으로 중분류 목록을 조회한다. CategoryMedium.major 가 @ManyToOne CategoryMajor 라고
 * 가정하고, 파생 쿼리 메서드로 major.id를 기준으로 조회한다.
 * </p>
 */
public interface CategoryMediumRepository extends JpaRepository<CategoryMedium, Long> {

    /**
     * 특정 대분류에 속한 중분류 목록 조회.
     * <P>
     * @EntityGraph로 categoryMajor를 함께 로딩해 N+1과 지연 초기화 이슈를 완화.
     * </P>
     * 
     * @param majorId 대분류 ID
     * @return 중분류 목록
     */
    @EntityGraph(attributePaths = "categoryMajor")
    List<CategoryMedium> findByCategoryMajor_Id(Long majorId);
}
