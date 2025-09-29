package com.bukadong.tcg.api.auction.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 내 경매 목록 아이템
 */
public record MyAuctionListItemResponse(Long auctionId, String code, String title, LocalDateTime startDatetime,
        LocalDateTime endDatetime, boolean isEnd, String closeReason, BigDecimal currentPrice, String imageUrl,
        List<BidItem> bids, DeliverySummary delivery) {
    public record BidItem(LocalDateTime time, String nickname, BigDecimal price) {
    }

    /**
     * 배송 요약 정보: 존재하면 status와 각 필드 존재 여부 제공, 없으면 null
     */
    public record DeliverySummary(String status, boolean existTrackingNumber, boolean existRecipientAddress,
            boolean existSenderAddress) {
    }
}
