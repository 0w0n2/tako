package com.bukadong.tcg.api.auction.entity;

/** 입찰 상태 */
public enum AuctionBidStatus {
    VALID, // 성공 반영
    REJECTED, // 조건 불충족(가격, 기간 등)
    FAILED, // 시스템/DB 오류
}
