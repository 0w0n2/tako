package com.bukadong.tcg.api.auction.service;

import java.util.Optional;

import com.bukadong.tcg.api.auction.service.dto.WinnerSnapshot;

/**
 * 낙찰자 조회 쿼리
 * <P>
 * 경매 종료 시 가장 높은 유효 입찰 스냅샷을 조회한다.
 * </P>
 * 
 * @PARAM auctionId 경매 ID
 * @RETURN Optional<WinnerSnapshot>
 */
public interface AuctionWinnerQuery {

    /**
     * 최고가 유효 입찰 스냅샷 조회
     * <P>
     * 없으면 Optional.empty()
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @RETURN Optional<WinnerSnapshot>
     */
    Optional<WinnerSnapshot> tryGetWinnerSnapshot(Long auctionId);
}
