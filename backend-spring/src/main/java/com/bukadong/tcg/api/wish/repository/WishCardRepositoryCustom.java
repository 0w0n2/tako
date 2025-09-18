package com.bukadong.tcg.api.wish.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bukadong.tcg.api.wish.dto.response.WishCardListRow;

/**
 * WishCard 커스텀 리포지토리
 * <P>
 * 관심 목록 프로젝션 조회.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public interface WishCardRepositoryCustom {
    Page<WishCardListRow> findMyWishCards(Long memberId, Pageable pageable);
}
