package com.bukadong.tcg.api.auction.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 내 경매 목록 아이템
 */
public record MyAuctionListItemResponse(Long auctionId, String code, String title, LocalDateTime startDatetime,
        LocalDateTime endDatetime, boolean isEnd, String closeReason, BigDecimal currentPrice, String imageUrl,
        List<BidItem> bids) {
    public record BidItem(LocalDateTime time, String nickname, BigDecimal price) {
    }
}
