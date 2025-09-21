package com.bukadong.tcg.api.card.repository;

import com.bukadong.tcg.api.card.entity.PhysicalCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

/**
 * 실물 카드 리포지토리
 */
public interface PhysicalCardRepository extends JpaRepository<PhysicalCard, Long> {
    Optional<PhysicalCard> findByTokenId(BigInteger tokenId);

    boolean existsByTokenId(BigInteger tokenId);
}
