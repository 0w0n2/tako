package com.bukadong.tcg.api.wish.repository.auction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bukadong.tcg.api.wish.dto.response.WishAuctionListRow;

/**
 * WishAuction 커스텀 리포지토리
 * <P>
 * 회원의 관심 경매 목록을 요약 정보로 페이지 조회.
 * </P>
 */
public interface WishAuctionRepositoryCustom {
    /**
     * 회원의 관심 경매 목록을 요약 정보로 페이지 조회 (원시 쿼리)
     * 
     * @param memberId
     * @param pageable
     * @return
     */
    Page<WishAuctionListRow> findMyWishAuctionsRaw(Long memberId, Pageable pageable);
}
