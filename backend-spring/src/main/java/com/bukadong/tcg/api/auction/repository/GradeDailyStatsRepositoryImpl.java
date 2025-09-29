package com.bukadong.tcg.api.auction.repository;

import com.bukadong.tcg.api.auction.dto.response.DailyGradePriceStatItemResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GradeDailyStatsRepositoryImpl implements GradeDailyStatsRepository {

    private final EntityManager em;

    @Override
    public List<DailyGradePriceStatItemResponse> findDailyStatsByCardId(Long cardId, int fromDays) {
        Instant fromStartUtc = LocalDate.now().minusDays(fromDays).atStartOfDay().toInstant(ZoneOffset.UTC);

        String sql = "SELECT DATE(ar.created_at) AS d, g.grade_code, "
                + "       MAX(ab.amount) AS max_amt, MIN(ab.amount) AS min_amt, AVG(ab.amount) AS avg_amt "
                + "  FROM auction_result ar " + "  JOIN auction a ON a.id = ar.auction_id "
                + "  JOIN card_ai_grade g ON g.id = a.grade_id " + "  JOIN auction_bid ab ON ab.id = ar.auction_bid_id "
                + " WHERE a.card_id = :cardId " + "   AND a.is_end = 1 " + "   AND ar.created_at >= :fromDate "
                + " GROUP BY DATE(ar.created_at), g.grade_code "
                + " ORDER BY DATE(ar.created_at) ASC, g.grade_code ASC";

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql).setParameter("cardId", cardId)
                .setParameter("fromDate", Timestamp.from(fromStartUtc)).getResultList();

        return rows.stream().map(r -> {
            LocalDate date = ((Date) r[0]).toLocalDate();
            String grade = (String) r[1];
            BigDecimal max = (BigDecimal) r[2];
            BigDecimal min = (BigDecimal) r[3];
            BigDecimal avg = (BigDecimal) r[4];
            return DailyGradePriceStatItemResponse.builder().date(date).grade(grade).amountMax(max).amountAvg(avg)
                    .amountMin(min).build();
        }).toList();
    }
}
