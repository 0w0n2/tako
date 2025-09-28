package com.bukadong.tcg.global.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 카드 FULLTEXT 인덱스 초기화(분리본)
 * <P>
 * 애플리케이션 시작 시 card 테이블에 다음 인덱스를 보장한다. - name 전용: ft_card_name (WITH PARSER
 * ngram) - description 전용: ft_card_desc (WITH PARSER ngram) 기존 복합
 * 인덱스(ft_card_name_desc)가 존재하면 경고 로그를 남긴다. 운영에서는 Flyway/Liquibase 사용을 권장하며, 이
 * 컴포넌트는 dev/stage 편의용이다. 활성화는 설정값 db.init.fulltext=true 로 제어한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "db.init.fulltext", havingValue = "true")
public class FullTextIndexInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FullTextIndexInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // name/description 전용 FULLTEXT 인덱스 보장
            ensureFullTextIndex("ft_card_name", "name");
            ensureFullTextIndex("ft_card_desc", "description");

            // 레거시 복합 인덱스 존재 시 경고
            Integer legacy = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM information_schema.statistics "
                    + "WHERE table_schema = DATABASE() AND table_name = 'card' AND index_name = 'ft_card_name_desc'",
                    Integer.class);
            if (legacy != null && legacy > 0) {
                log.warn("[FullTextIndexInitializer] Legacy FULLTEXT index 'ft_card_name_desc' detected. "
                        + "Prefer split indexes 'ft_card_name' & 'ft_card_desc'. Consider dropping the legacy index via migration.");
            }

            // 생성 직후 통계 갱신
            jdbcTemplate.execute("ANALYZE TABLE card");
        } catch (Exception e) {
            log.warn("[FullTextIndexInitializer] Failed to ensure FULLTEXT indexes: {}", e.getMessage());
        }
    }

    /**
     * FULLTEXT 인덱스 존재 확인 후 없으면 생성
     * <P>
     * WITH PARSER ngram을 사용하여 MySQL ngram 파서 기반으로 생성한다.
     * </P>
     * 
     * @PARAM indexName 생성할 인덱스명
     * @PARAM columnList 대상 컬럼 목록(예: "name" 또는 "description")
     * @RETURN 없음
     */
    private void ensureFullTextIndex(String indexName, String columnList) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.statistics "
                        + "WHERE table_schema = DATABASE() AND table_name = 'card' AND index_name = ?",
                Integer.class, indexName);

        if (exists == null || exists == 0) {
            log.info("[FullTextIndexInitializer] Creating FULLTEXT index {} on card({}) ...", indexName, columnList);
            String ddl = String.format("CREATE FULLTEXT INDEX %s ON card (%s) WITH PARSER ngram", indexName,
                    columnList);
            jdbcTemplate.execute(ddl);
            log.info("[FullTextIndexInitializer] FULLTEXT index {} created.", indexName);
        } else {
            log.info("[FullTextIndexInitializer] FULLTEXT index {} already exists.", indexName);
        }
    }
}
