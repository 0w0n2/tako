package com.bukadong.tcg.api.auction.controller;

import com.bukadong.tcg.api.auction.dto.response.GradeDailyPriceStatsResponse;
import com.bukadong.tcg.api.auction.service.GradeDailyStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auctions")
@RequestMapping("/v1/auctions")
public class GradeDailyStatsController {

    private final GradeDailyStatsService service;

    @Operation(summary = "카드 등급별 일자별 낙찰가 통계", description = "cardId 기준 최근 N일(기본 7일) 동안 낙찰 완료 경매를 등급별로 일자별 집계하여 (최고/평균/최저)를 반환합니다.")
    @GetMapping("/cards/{cardId}/grade-daily-stats")
    public List<GradeDailyPriceStatsResponse> getGradeDailyStats(
            @Parameter(description = "카드 ID") @PathVariable("cardId") Long cardId,
            @Parameter(description = "최근 N일", example = "7") @RequestParam(name = "days", required = false, defaultValue = "7") Integer days) {
        return service.getStats(cardId, days);
    }
}
