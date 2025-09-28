package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.dto.response.DailyGradePriceStatItemResponse;
import com.bukadong.tcg.api.auction.dto.response.GradeDailyPriceStatsResponse;
import com.bukadong.tcg.api.auction.repository.GradeDailyStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeDailyStatsService {

    private final GradeDailyStatsRepository repository;

    public List<GradeDailyPriceStatsResponse> getStats(Long cardId, Integer days) {
        int range = (days == null || days <= 0) ? 7 : days;
        List<DailyGradePriceStatItemResponse> rows = repository.findDailyStatsByCardId(cardId, range);

        // 날짜 범위: 오늘 포함 최근 range일 (LocalDate, 내림차순 출력 목표이지만 채우기는 전체 set 필요)
        LocalDate today = LocalDate.now();
        List<LocalDate> allDatesDesc = IntStream.range(0, range).mapToObj(today::minusDays).toList();

        // 등급별로 묶기
        Map<String, List<DailyGradePriceStatItemResponse>> grouped = new LinkedHashMap<>();
        for (DailyGradePriceStatItemResponse item : rows) {
            grouped.computeIfAbsent(item.getGrade(), g -> new ArrayList<>()).add(item);
        }

        // 각 등급에 대해 누락 날짜 채우고 최신순 정렬
        List<GradeDailyPriceStatsResponse> result = new ArrayList<>();
        for (Map.Entry<String, List<DailyGradePriceStatItemResponse>> entry : grouped.entrySet()) {
            String grade = entry.getKey();
            // 기존 데이터: date -> item 맵 (있으면 사용)
            Map<LocalDate, DailyGradePriceStatItemResponse> byDate = entry.getValue().stream()
                    .collect(Collectors.toMap(DailyGradePriceStatItemResponse::getDate, it -> it, (a, b) -> a));

            List<DailyGradePriceStatItemResponse> filled = new ArrayList<>(allDatesDesc.size());
            for (LocalDate d : allDatesDesc) {
                DailyGradePriceStatItemResponse exist = byDate.get(d);
                if (exist != null) {
                    filled.add(exist);
                } else {
                    // 빈 날짜: 금액 null, grade는 내부 유지
                    filled.add(DailyGradePriceStatItemResponse.builder().date(d).grade(grade).amountMax(null)
                            .amountAvg(null).amountMin(null).build());
                }
            }

            // 이미 allDatesDesc는 최신순이므로 별도 정렬 불필요. 혹시 몰라 내림차순 보장 처리
            filled.sort(Comparator.comparing(DailyGradePriceStatItemResponse::getDate).reversed());

            result.add(GradeDailyPriceStatsResponse.builder().grade(grade).items(filled).build());
        }

        // 등급 그룹 자체는 입력 순서를 유지 (LinkedHashMap). 필요시 등급 정렬 규칙 추가 가능.
        return result;
    }
}
