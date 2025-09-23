package com.bukadong.tcg.api.auction.repository;

/**
 * 경매 결과 QueryDSL 검색 리포지토리
 */
public interface AuctionResultRepositoryCustom {

    String findSettleTxHash(Long auctionId);

}
