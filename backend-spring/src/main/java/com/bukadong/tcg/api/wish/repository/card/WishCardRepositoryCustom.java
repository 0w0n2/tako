package com.bukadong.tcg.api.wish.repository.card;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bukadong.tcg.api.wish.dto.response.WishCardListRow;

/**
 * WishCard 커스텀 리포지토리
 * <P>
 * 관심 목록 프로젝션 조회.
 * </P>
 */
public interface WishCardRepositoryCustom {
    /**
     * 회원의 관심 카드 목록을 요약 정보로 페이지 조회
     * 
     * @param memberId
     * @param pageable
     * @return
     */
    Page<WishCardListRow> findMyWishCards(Long memberId, Pageable pageable);
}
