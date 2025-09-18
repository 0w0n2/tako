package com.bukadong.tcg.api.wish.repository.card;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bukadong.tcg.api.wish.entity.WishCard;

import java.util.Optional;

/**
 * WishCard JPA 리포지토리
 * <P>
 * memberId+cardId 단건 조회 및 upsert 용도.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public interface WishCardRepository extends JpaRepository<WishCard, Long>, WishCardRepositoryCustom {
    Optional<WishCard> findByMemberIdAndCardId(Long memberId, Long cardId);
}
