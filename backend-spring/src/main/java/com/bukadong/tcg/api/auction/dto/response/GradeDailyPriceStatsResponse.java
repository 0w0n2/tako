package com.bukadong.tcg.api.auction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeDailyPriceStatsResponse {

    @Schema(description = "등급 코드", example = "PSA10")
    private String grade;

    @Schema(description = "해당 등급의 일자별 통계")
    private List<DailyGradePriceStatItemResponse> items;
}
