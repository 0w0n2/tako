package com.bukadong.tcg.api.card.repository.custom;

import com.bukadong.tcg.api.card.dto.response.CardDetailResponse;
import com.bukadong.tcg.api.card.dto.response.CardListRow;
import com.bukadong.tcg.api.card.entity.CardAttribute;
import com.bukadong.tcg.api.card.entity.QCard;
import com.bukadong.tcg.api.card.entity.Rarity;
import com.bukadong.tcg.api.card.util.FullTextBooleanQuery;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 카드 커스텀 검색 구현
 * <P>
 * 중요 로직: - descriptionKeyword가 있으면 FULLTEXT 점수 기반 정렬 - namePrefix는 LIKE
 * 'name%'로 인덱스 사용 - 카운트 쿼리 분리(QueryDSL 규약)
 * </P>
 * 
 * @PARAM cond 검색 조건
 * @PARAM pageable 페이지 정보
 * @RETURN Page<CardListRow>
 */
@Repository
@RequiredArgsConstructor
public class CardRepositoryImpl implements CardRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager em;

    private JdbcTemplate jdbcTemplate;

    private int detectedNgramTokenSize = 2;

    @PostConstruct
    void initNgram() {
        try {
            Integer v = jdbcTemplate.queryForObject("SELECT @@ngram_token_size", Integer.class);
            if (v != null && v >= 1 && v <= 10)
                detectedNgramTokenSize = v;
        } catch (Exception ignored) {
            /* default 2 */ }
    }

    @Override
    public Page<CardListRow> search(CardSearchCond cond, Pageable pageable) {
        QCard c = QCard.card;

        // 키워드 존재 여부 판단
        final String rawName = (cond.getNameKeyword() == null) ? null : cond.getNameKeyword().trim();
        final String rawDesc = (cond.getDescriptionKeyword() == null) ? null : cond.getDescriptionKeyword().trim();
        final boolean hasName = rawName != null && !rawName.isEmpty();
        final boolean hasDesc = rawDesc != null && !rawDesc.isEmpty();

        if (hasName || hasDesc) {
            // --- FULLTEXT 또는 LIKE 폴백 경로 (네이티브 SQL) ---
            final int ngram = detectedNgramTokenSize;
            final String qName = hasName ? FullTextBooleanQuery.buildForMySQLNgram(rawName, ngram) : null;
            final String qDesc = hasDesc ? FullTextBooleanQuery.buildForMySQLNgram(rawDesc, ngram) : null;

            final boolean useLikeName = hasName && (qName == null);
            final boolean useLikeDesc = hasDesc && (qDesc == null);

            StringBuilder select = new StringBuilder().append("SELECT c.id, c.name, c.code, c.attribute, c.rarity, ");

            // 점수 계산: FT 점수들의 합 + LIKE 가중치
            List<String> scoreParts = new java.util.ArrayList<>();
            if (hasName && !useLikeName)
                scoreParts.add("IFNULL(MATCH(c.name) AGAINST (:qn IN BOOLEAN MODE), 0)");
            if (hasDesc && !useLikeDesc)
                scoreParts.add("IFNULL(MATCH(c.description) AGAINST (:qd IN BOOLEAN MODE), 0)");
            if (useLikeName)
                scoreParts.add("CASE WHEN c.name LIKE :likeName THEN 0.5 ELSE 0 END");
            if (useLikeDesc)
                scoreParts.add("CASE WHEN c.description LIKE :likeDesc THEN 0.5 ELSE 0 END");

            if (scoreParts.isEmpty())
                scoreParts.add("0");
            select.append(String.join(" + ", scoreParts)).append(" AS score ");

            StringBuilder fromWhere = new StringBuilder("FROM card c WHERE 1=1 ");

            StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM card c WHERE 1=1 ");

            // 공통 필터
            if (cond.getCategoryMajorId() != null) {
                fromWhere.append(" AND c.category_major_id = :majorId ");
                countSql.append(" AND c.category_major_id = :majorId ");
            }
            if (cond.getCategoryMediumId() != null) {
                fromWhere.append(" AND c.category_medium_id = :mediumId ");
                countSql.append(" AND c.category_medium_id = :mediumId ");
            }

            // name FT / LIKE 조건
            if (hasName && !useLikeName) {
                fromWhere.append(" AND MATCH(c.name) AGAINST (:qn IN BOOLEAN MODE) ");
                countSql.append(" AND MATCH(c.name) AGAINST (:qn IN BOOLEAN MODE) ");
            }
            if (useLikeName) {
                fromWhere.append(" AND c.name LIKE :likeName ");
                countSql.append(" AND c.name LIKE :likeName ");
            }

            // desc FT / LIKE 조건
            if (hasDesc && !useLikeDesc) {
                fromWhere.append(" AND MATCH(c.description) AGAINST (:qd IN BOOLEAN MODE) ");
                countSql.append(" AND MATCH(c.description) AGAINST (:qd IN BOOLEAN MODE) ");
            }
            if (useLikeDesc) {
                fromWhere.append(" AND c.description LIKE :likeDesc ");
                countSql.append(" AND c.description LIKE :likeDesc ");
            }

            String orderLimit = " ORDER BY score DESC, c.id DESC LIMIT :limit OFFSET :offset";

            var q = em.createNativeQuery(select.toString() + fromWhere.toString() + orderLimit);
            var qc = em.createNativeQuery(countSql.toString());

            // 바인딩
            if (cond.getCategoryMajorId() != null) {
                q.setParameter("majorId", cond.getCategoryMajorId());
                qc.setParameter("majorId", cond.getCategoryMajorId());
            }
            if (cond.getCategoryMediumId() != null) {
                q.setParameter("mediumId", cond.getCategoryMediumId());
                qc.setParameter("mediumId", cond.getCategoryMediumId());
            }

            if (hasName && !useLikeName) {
                q.setParameter("qn", qName);
                qc.setParameter("qn", qName);
            }
            if (useLikeName) {
                q.setParameter("likeName", "%" + rawName + "%");
                qc.setParameter("likeName", "%" + rawName + "%");
            }

            if (hasDesc && !useLikeDesc) {
                q.setParameter("qd", qDesc);
                qc.setParameter("qd", qDesc);
            }
            if (useLikeDesc) {
                q.setParameter("likeDesc", "%" + rawDesc + "%");
                qc.setParameter("likeDesc", "%" + rawDesc + "%");
            }

            q.setParameter("limit", pageable.getPageSize());
            q.setParameter("offset", (int) pageable.getOffset());

            @SuppressWarnings("unchecked")
            List<Object[]> rows = q.getResultList();

            // 0건이면 최후의 방어선: 둘 다 LIKE로 재시도(UX 보완)
            if (rows.isEmpty() && (hasName || hasDesc)) {
                var fb = new StringBuilder().append("SELECT c.id, c.name, c.code, c.attribute, c.rarity, 0.0 AS score ")
                        .append("FROM card c WHERE 1=1 ");
                if (cond.getCategoryMajorId() != null)
                    fb.append(" AND c.category_major_id = :majorId ");
                if (cond.getCategoryMediumId() != null)
                    fb.append(" AND c.category_medium_id = :mediumId ");
                if (hasName)
                    fb.append(" AND c.name LIKE :likeName ");
                if (hasDesc)
                    fb.append(" AND c.description LIKE :likeDesc ");
                fb.append(" ORDER BY c.id DESC LIMIT :limit OFFSET :offset ");
                var qfb = em.createNativeQuery(fb.toString());
                if (cond.getCategoryMajorId() != null)
                    qfb.setParameter("majorId", cond.getCategoryMajorId());
                if (cond.getCategoryMediumId() != null)
                    qfb.setParameter("mediumId", cond.getCategoryMediumId());
                if (hasName)
                    qfb.setParameter("likeName", "%" + rawName + "%");
                if (hasDesc)
                    qfb.setParameter("likeDesc", "%" + rawDesc + "%");
                qfb.setParameter("limit", pageable.getPageSize());
                qfb.setParameter("offset", (int) pageable.getOffset());
                rows = qfb.getResultList();
                // count는 기존 qc 결과를 그대로 사용(대체 가능하지만 성능상 재사용)
            }

            List<CardListRow> content = new ArrayList<>(rows.size());
            for (Object[] r : rows) {
                Long id = ((Number) r[0]).longValue();
                String name = (String) r[1];
                String code = (String) r[2];
                CardAttribute attribute = CardAttribute.valueOf((String) r[3]);
                Rarity rarity = Rarity.valueOf((String) r[4]);
                Double score = (r[5] == null) ? 0.0 : ((Number) r[5]).doubleValue();

                content.add(new CardListRow(id, name, code, attribute, rarity, score));
            }

            Number totalNum = (Number) qc.getSingleResult();
            long total = totalNum == null ? 0L : totalNum.longValue();
            return new PageImpl<>(content, pageable, total);
        }

        // ---- 키워드가 전혀 없을 때: 기존 QueryDSL 경로 (id desc) ----
        BooleanBuilder where = new BooleanBuilder();
        if (cond.getCategoryMajorId() != null)
            where.and(c.categoryMajor.id.eq(cond.getCategoryMajorId()));
        if (cond.getCategoryMediumId() != null)
            where.and(c.categoryMedium.id.eq(cond.getCategoryMediumId()));

        List<CardListRow> content = queryFactory
                .select(Projections.constructor(CardListRow.class, c.id, c.name, c.code, c.attribute, c.rarity,
                        Expressions.constant(0.0)))
                .from(c).where(where).orderBy(c.id.desc()).offset(pageable.getOffset()).limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory.select(c.count()).from(c).where(where).fetchOne();
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public CardDetailResponse findDetailById(Long id) {
        final QCard card = QCard.card;

        return queryFactory
                .select(Projections.fields(CardDetailResponse.class, card.id.as("id"),
                        card.categoryMajor.id.as("categoryMajorId"), card.categoryMedium.id.as("categoryMediumId"),
                        card.code.as("code"), card.name.as("name"), card.description.as("description"),
                        card.attribute.stringValue().as("attribute"), card.rarity.stringValue().as("rarity")))
                .from(card).where(card.id.eq(id)).fetchOne(); // 없으면 null
    }
}
