package com.bukadong.tcg.api.card.repository;

import com.bukadong.tcg.api.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
