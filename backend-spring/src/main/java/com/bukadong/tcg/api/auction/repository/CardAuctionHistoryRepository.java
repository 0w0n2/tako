package com.bukadong.tcg.api.auction.repository;

import com.bukadong.tcg.api.auction.dto.response.CardAuctionHistoryItemResponse;
import java.util.List;

/**
 * 카드별 낙찰 이력 조회 리포지토리 (읽기 전용)
 */
public interface CardAuctionHistoryRepository {

    /**
     * 카드 ID 기준 낙찰 완료 이력 조회 - 기간: fromDays 일 전부터 현재까지 - 정렬: 최근순(desc)
     */
    List<CardAuctionHistoryItemResponse> findCompletedHistoriesByCardId(Long cardId, int fromDays);
}
