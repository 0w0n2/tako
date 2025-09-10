package com.bukadong.tcg.api.auction.repository;

/**
 * 경매 목록 정렬 기준
 * <p>
 * 마감시간/입찰수 기준 정렬을 표현한다.
 * </p>
 */
public enum AuctionSort {
    /** 마감 임박순(가까운 순) */
    ENDTIME_ASC,
    /** 마감 느린순(먼 순) */
    ENDTIME_DESC,
    /** 입찰 많은순 */
    BIDCOUNT_DESC,
    /** 입찰 적은순 */
    BIDCOUNT_ASC
}
