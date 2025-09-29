package com.bukadong.tcg.api.auction.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyGradePriceStatItemResponse {

    @Schema(description = "집계 일자", example = "2025-09-27")
    private LocalDate date;

    @Schema(description = "등급 코드", example = "PSA10")
    @JsonIgnore
    private String grade;

    @Schema(description = "최고가")
    private BigDecimal amountMax;

    @Schema(description = "평균가")
    private BigDecimal amountAvg;

    @Schema(description = "최저가")
    private BigDecimal amountMin;
}
