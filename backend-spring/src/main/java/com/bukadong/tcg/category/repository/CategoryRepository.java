package com.bukadong.tcg.category.repository;

import com.bukadong.tcg.category.entity.CategoryMajor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 카테고리 엔티티({@link CategoryMajor})용 Spring Data JPA 레포지토리.
 *
 * <p>
 * 기본적인 CRUD 연산은 {@link JpaRepository}를 통해 제공되며,
 * 추가로 코드(code) 기반 단건 조회 메서드를 제공한다.
 * </p>
 */
public interface CategoryRepository extends JpaRepository<CategoryMajor, Long> {

    /**
     * 카테고리 코드(code)로 카테고리를 단건 조회한다.
     *
     * @param code 카테고리 고유 코드 (Unique)
     * @return 해당 코드의 카테고리가 존재하면 {@link Optional}에 담아 반환,
     *         존재하지 않으면 빈 {@link Optional}
     */
    Optional<CategoryMajor> findByCode(String code);
}
