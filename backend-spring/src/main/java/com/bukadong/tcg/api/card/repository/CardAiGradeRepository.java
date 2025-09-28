package com.bukadong.tcg.api.card.repository;

import com.bukadong.tcg.api.card.entity.CardAiGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 카드 AI 등급 리포지토리
 * <P>
 * hash로 단건 조회 지원.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public interface CardAiGradeRepository extends JpaRepository<CardAiGrade, Long> {

    Optional<CardAiGrade> findByHash(String hash);
}
