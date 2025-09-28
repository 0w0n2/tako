package com.bukadong.tcg.api.auction.repository;

import com.bukadong.tcg.api.auction.dto.response.DailyGradePriceStatItemResponse;

import java.util.List;

public interface GradeDailyStatsRepository {

    /**
     * cardId에 대해 최근 fromDays 일 범위에서, 낙찰 완료 경매의 일자별/등급별 (max/avg/min) 집계 결과를 반환한다.
     * 결과는 날짜 오름차순으로 정렬된다.
     */
    List<DailyGradePriceStatItemResponse> findDailyStatsByCardId(Long cardId, int fromDays);
}
