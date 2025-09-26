package com.bukadong.tcg.api.auction.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bukadong.tcg.api.auction.entity.CardCondition;
import com.bukadong.tcg.api.auction.entity.DescriptionMatch;
import com.bukadong.tcg.api.auction.entity.PriceSatisfaction;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * /v1/reviews/me 응답 아이템
 * - /v1/auctions/mybid 형식과 /v1/reviews/{memberId}/reviews를 조합
 */
public record MyReviewItemResponse(
        Long auctionId,
        String code,
        String title,
        LocalDateTime startDatetime,
        LocalDateTime endDatetime,
        boolean isEnd,
        String closeReason,
        BigDecimal currentPrice,
        String imageUrl,
        DeliverySummary delivery,
        ReviewSummary review // done=true일 때만 채움, 아니면 null
) {

    /** 배송 요약 (구매자 낙찰자 기준 노출) */
    public record DeliverySummary(
            @Schema(description = "배송 상태") String status,
            @Schema(description = "운송장 존재 여부") boolean existTrackingNumber,
            @Schema(description = "받는 주소 존재 여부") boolean existRecipientAddress,
            @Schema(description = "보내는 주소 존재 여부") boolean existSenderAddress
    ) {}

    /** 리뷰 요약 (작성자 닉네임은 일부 마스킹) */
    public record ReviewSummary(
            Long id,
            String writerMaskedNickname,
            String reviewText,
            CardCondition cardCondition,
            PriceSatisfaction priceSatisfaction,
            DescriptionMatch descriptionMatch,
            int star,
            LocalDateTime createdAt
    ) {}
}
