package com.bukadong.tcg.api.auction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.bukadong.tcg.api.bid.repository.AuctionBidRepository;

/**
 * 낙찰 정보 조회 구현
 * <P>
 * 최고가 → 최신 시간 순으로 1건을 조회한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Service
@RequiredArgsConstructor
public class AuctionWinnerQueryImpl implements AuctionWinnerQuery {

    private final AuctionBidRepository bidRepository;

    @Override
    public WinnerSnapshot getWinnerSnapshot(long auctionId) {
        var top = bidRepository.findTopByAuctionIdOrderByBidPriceDescCreatedAtDesc(auctionId)
                .orElseThrow(() -> new IllegalStateException("No bids for auction " + auctionId));
        return new WinnerSnapshot(top.getMember().getId(), top.getBidPrice(), top.getId());
    }
}
