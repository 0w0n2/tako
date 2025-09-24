package com.bukadong.tcg.api.auction.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 내가 입찰중인 경매 목록 아이템 (판매 목록 형식 + 내 최고가 추가)
 */
public record MyBidAuctionListItemResponse(Long auctionId, String code, String title, LocalDateTime startDatetime,
        LocalDateTime endDatetime, boolean isEnd, String closeReason, BigDecimal currentPrice,
        BigDecimal myTopBidAmount, String imageUrl, List<BidItem> bids, DeliverySummary delivery) {
    public record BidItem(LocalDateTime time, String nickname, BigDecimal price) {
    }

    /** 배송 요약 (내가 낙찰자인 경우에만 포함, 아니면 null) */
    public record DeliverySummary(String status, boolean existTrackingNumber, boolean existRecipientAddress,
            boolean existSenderAddress) {
    }
}
