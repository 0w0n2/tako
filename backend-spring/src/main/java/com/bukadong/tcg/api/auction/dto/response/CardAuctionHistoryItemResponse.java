package com.bukadong.tcg.api.auction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 카드별 낙찰 이력 응답 아이템
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardAuctionHistoryItemResponse {

    @Schema(description = "낙찰 시각", example = "2025-09-15T16:12:30")
    private LocalDateTime successfulBidTime;

    @Schema(description = "등급 코드", example = "PSA10")
    private String grade;

    @Schema(description = "낙찰 금액", example = "12000.00")
    private BigDecimal amount;
}
