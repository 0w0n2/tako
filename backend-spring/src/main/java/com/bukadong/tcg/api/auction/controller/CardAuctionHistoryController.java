package com.bukadong.tcg.api.auction.controller;

import com.bukadong.tcg.api.auction.dto.response.CardAuctionHistoryItemResponse;
import com.bukadong.tcg.api.auction.service.CardAuctionHistoryService;
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
public class CardAuctionHistoryController {

    private final CardAuctionHistoryService service;

    @Operation(summary = "카드별 낙찰 이력 조회", description = "cardId로 낙찰 완료 경매 이력을 최근순으로 조회합니다. 기간 기본값 7일.")
    @GetMapping("/cards/{cardId}/history")
    public List<CardAuctionHistoryItemResponse> getCardAuctionHistory(
            @Parameter(description = "카드 ID") @PathVariable("cardId") Long cardId,
            @Parameter(description = "최근 N일", example = "7") @RequestParam(name = "days", required = false, defaultValue = "7") Integer days) {
        return service.getCompletedHistories(cardId, days);
    }
}
