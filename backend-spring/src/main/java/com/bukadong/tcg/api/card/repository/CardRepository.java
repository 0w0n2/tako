package com.bukadong.tcg.api.card.repository;

import com.bukadong.tcg.api.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 카드 리포지토리
 * <P>
 * 인기 카드 응답에 필요한 최소 조회를 담당한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public interface CardRepository extends JpaRepository<Card, Long> {
    @Query("select count(c) from Card c where c.categoryMajor.id = :majorId")
    long countByCategoryMajorId(@Param("majorId") Long majorId);

    @Query("select count(c) from Card c where c.categoryMedium.id = :mediumId")
    long countByCategoryMediumId(@Param("mediumId") Long mediumId);
}
