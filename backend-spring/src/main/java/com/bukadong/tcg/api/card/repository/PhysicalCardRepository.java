package com.bukadong.tcg.api.card.repository;

import com.bukadong.tcg.api.card.entity.PhysicalCard;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 실물 카드 리포지토리
 */
public interface PhysicalCardRepository extends JpaRepository<PhysicalCard, Long> {
}
