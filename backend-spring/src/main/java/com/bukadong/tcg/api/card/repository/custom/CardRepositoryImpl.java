package com.bukadong.tcg.api.card.repository.custom;

import com.bukadong.tcg.api.card.dto.response.CardDetailResponse;
import com.bukadong.tcg.api.card.dto.response.CardListRow;
import com.bukadong.tcg.api.card.entity.CardAttribute;
import com.bukadong.tcg.api.card.entity.QCard;
import com.bukadong.tcg.api.card.entity.Rarity;
import com.bukadong.tcg.api.card.util.FullTextBooleanQuery;
import com.bukadong.tcg.api.wish.entity.QWishCard;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 카드 커스텀 검색 구현
 * <p>
 * 중요 로직: - descriptionKeyword가 있으면 FULLTEXT 점수 기반 정렬 - namePrefix는 LIKE 'name%'
 * 로 인덱스 사용 - 카운트 쿼리 분리(QueryDSL 규약)
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class CardRepositoryImpl implements CardRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager em;

    private JdbcTemplate jdbcTemplate;

    private int detectedNgramTokenSize = 2;

    private static final Logger log = LoggerFactory.getLogger(CardRepositoryImpl.class);

    // WHERE 절 상수화
    private static final String WHERE_MAJOR_ID = " AND c.category_major_id = :majorId ";
    private static final String WHERE_MEDIUM_ID = " AND c.category_medium_id = :mediumId ";
    private static final String WHERE_MATCH_NAME = " AND MATCH(c.name) AGAINST (:qn IN BOOLEAN MODE) ";
    private static final String WHERE_MATCH_DESC = " AND MATCH(c.description) AGAINST (:qd IN BOOLEAN MODE) ";
    private static final String WHERE_LIKE_NAME = " AND c.name LIKE :likeName ";
    private static final String WHERE_LIKE_DESC = " AND c.description LIKE :likeDesc ";

    // 파라미터 이름 상수화
    private static final String PARAM_MAJOR_ID = "majorId";
    private static final String PARAM_MEDIUM_ID = "mediumId";
    private static final String PARAM_QN = "qn";
    private static final String PARAM_QD = "qd";
    private static final String PARAM_LIKE_NAME = "likeName";
    private static final String PARAM_LIKE_DESC = "likeDesc";
    private static final String PARAM_LIMIT = "limit";
    private static final String PARAM_OFFSET = "offset";
    private static final String PARAM_MEMBER_ID = "memberId";

    @PostConstruct
    void initNgram() {
        try {
            Integer v = jdbcTemplate != null ? jdbcTemplate.queryForObject("SELECT @@ngram_token_size", Integer.class)
                    : null;
            if (v != null && v >= 1 && v <= 10)
                detectedNgramTokenSize = v;
        } catch (Exception ignored) {
            // default 2
        }
    }

    @Override
    public Page<CardListRow> search(CardSearchCond cond, Pageable pageable, Long memberId) {
        final String rawName = (cond.getNameKeyword() == null) ? null : cond.getNameKeyword().trim();
        final String rawDesc = (cond.getDescriptionKeyword() == null) ? null : cond.getDescriptionKeyword().trim();
        final boolean hasName = rawName != null && !rawName.isEmpty();
        final boolean hasDesc = rawDesc != null && !rawDesc.isEmpty();
        final Long memberParam = (memberId == null ? -1L : memberId); // 비로그인 방지용

        if (hasName || hasDesc) {
            return searchWithKeywords(cond, pageable, memberParam, rawName, rawDesc, hasName, hasDesc);
        }
        return searchWithoutKeywords(cond, pageable, memberParam);
    }

    private Page<CardListRow> searchWithKeywords(CardSearchCond cond, Pageable pageable, Long memberParam,
            String rawName, String rawDesc, boolean hasName, boolean hasDesc) {
        final int ngram = detectedNgramTokenSize;
        final String qName = hasName ? FullTextBooleanQuery.buildForMySQLNgram(rawName, ngram) : null;
        final String qDesc = hasDesc ? FullTextBooleanQuery.buildForMySQLNgram(rawDesc, ngram) : null;

        final boolean useLikeName = hasName && (qName == null);
        final boolean useLikeDesc = hasDesc && (qDesc == null);

        StringBuilder select = new StringBuilder()
                .append(buildSelectClause(hasName, useLikeName, hasDesc, useLikeDesc));

        StringBuilder fromWhere = new StringBuilder()
                .append("FROM card c ")
                .append("LEFT JOIN wish_card wc ")
                .append("  ON wc.card_id = c.id ")
                .append(" AND wc.member_id = :memberId ")
                .append(" AND wc.wish_flag = b'1' ")
                .append("WHERE 1=1 ");

        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM card c WHERE 1=1 ");

        appendCommonFilters(fromWhere, countSql, cond);
        appendNameDescFilters(fromWhere, countSql, hasName, hasDesc, useLikeName, useLikeDesc);

        String orderLimit = " ORDER BY score DESC, c.id DESC LIMIT :limit OFFSET :offset";

        Query q = em.createNativeQuery(select.toString() + fromWhere + orderLimit);
        Query qc = em.createNativeQuery(countSql.toString());

        bindParameters(q, qc, cond, rawName, rawDesc, hasName, hasDesc, useLikeName, useLikeDesc, qName, qDesc,
                pageable, memberParam);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        rows = executeLikeFallbackIfEmpty(rows, cond, pageable, memberParam, hasName, hasDesc, rawName, rawDesc);

        List<CardListRow> content = mapRowsToCardList(rows);

        Number totalNum = (Number) qc.getSingleResult();
        long total = totalNum == null ? 0L : totalNum.longValue();
        return new PageImpl<>(content, pageable, total);
    }

    private Page<CardListRow> searchWithoutKeywords(CardSearchCond cond, Pageable pageable, Long memberParam) {
        QCard c = QCard.card;
        BooleanBuilder where = new BooleanBuilder();
        if (cond.getCategoryMajorId() != null)
            where.and(c.categoryMajor.id.eq(cond.getCategoryMajorId()));
        if (cond.getCategoryMediumId() != null)
            where.and(c.categoryMedium.id.eq(cond.getCategoryMediumId()));

        QWishCard wc = QWishCard.wishCard;
        BooleanExpression wishedExpr = JPAExpressions.selectOne().from(wc)
                .where(wc.cardId.eq(c.id)
                        .and(wc.memberId.eq(memberParam))
                        .and(wc.wishFlag.isTrue()))
                .exists();

        List<CardListRow> content = queryFactory
                .select(Projections.constructor(CardListRow.class,
                        c.id, c.name, c.code, c.attribute, c.rarity,
                        Expressions.constant(0.0), wishedExpr))
                .from(c)
                .where(where)
                .orderBy(c.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory.select(c.count()).from(c).where(where).fetchOne();
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private String buildSelectClause(boolean hasName, boolean useLikeName, boolean hasDesc, boolean useLikeDesc) {
        StringBuilder select = new StringBuilder().append("SELECT c.id, c.name, c.code, c.attribute, c.rarity, ");
        List<String> scoreParts = new ArrayList<>();
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
        select.append(String.join(" + ", scoreParts)).append(" AS score, ");
        select.append("CASE WHEN wc.id IS NULL THEN 0 ELSE 1 END AS wished ");
        return select.toString();
    }

    private void appendCommonFilters(StringBuilder fromWhere, StringBuilder countSql, CardSearchCond cond) {
        if (cond.getCategoryMajorId() != null) {
            fromWhere.append(WHERE_MAJOR_ID);
            countSql.append(WHERE_MAJOR_ID);
        }
        if (cond.getCategoryMediumId() != null) {
            fromWhere.append(WHERE_MEDIUM_ID);
            countSql.append(WHERE_MEDIUM_ID);
        }
    }

    private void appendNameDescFilters(StringBuilder fromWhere, StringBuilder countSql,
            boolean hasName, boolean hasDesc, boolean useLikeName, boolean useLikeDesc) {
        if (hasName && !useLikeName) {
            fromWhere.append(WHERE_MATCH_NAME);
            countSql.append(WHERE_MATCH_NAME);
        }
        if (useLikeName) {
            fromWhere.append(WHERE_LIKE_NAME);
            countSql.append(WHERE_LIKE_NAME);
        }
        if (hasDesc && !useLikeDesc) {
            fromWhere.append(WHERE_MATCH_DESC);
            countSql.append(WHERE_MATCH_DESC);
        }
        if (useLikeDesc) {
            fromWhere.append(WHERE_LIKE_DESC);
            countSql.append(WHERE_LIKE_DESC);
        }
    }

    private void bindParameters(Query q, Query qc, CardSearchCond cond,
            String rawName, String rawDesc, boolean hasName, boolean hasDesc,
            boolean useLikeName, boolean useLikeDesc,
            String qName, String qDesc, Pageable pageable, Long memberParam) {
        if (cond.getCategoryMajorId() != null) {
            q.setParameter(PARAM_MAJOR_ID, cond.getCategoryMajorId());
            qc.setParameter(PARAM_MAJOR_ID, cond.getCategoryMajorId());
        }
        if (cond.getCategoryMediumId() != null) {
            q.setParameter(PARAM_MEDIUM_ID, cond.getCategoryMediumId());
            qc.setParameter(PARAM_MEDIUM_ID, cond.getCategoryMediumId());
        }
        if (hasName && !useLikeName) {
            q.setParameter(PARAM_QN, qName);
            qc.setParameter(PARAM_QN, qName);
        }
        if (useLikeName) {
            q.setParameter(PARAM_LIKE_NAME, "%" + rawName + "%");
            qc.setParameter(PARAM_LIKE_NAME, "%" + rawName + "%");
        }
        if (hasDesc && !useLikeDesc) {
            q.setParameter(PARAM_QD, qDesc);
            qc.setParameter(PARAM_QD, qDesc);
        }
        if (useLikeDesc) {
            q.setParameter(PARAM_LIKE_DESC, "%" + rawDesc + "%");
            qc.setParameter(PARAM_LIKE_DESC, "%" + rawDesc + "%");
        }
        q.setParameter(PARAM_LIMIT, pageable.getPageSize());
        q.setParameter(PARAM_OFFSET, (int) pageable.getOffset());
        q.setParameter(PARAM_MEMBER_ID, memberParam);
    }

    private List<Object[]> executeLikeFallbackIfEmpty(List<Object[]> rows, CardSearchCond cond, Pageable pageable,
            Long memberParam, boolean hasName, boolean hasDesc,
            String rawName, String rawDesc) {
        if (!rows.isEmpty() || !(hasName || hasDesc))
            return rows;

        StringBuilder fb = new StringBuilder()
                .append("SELECT c.id, c.name, c.code, c.attribute, c.rarity, 0.0 AS score, ")
                .append("CASE WHEN wc.id IS NULL THEN 0 ELSE 1 END AS wished ")
                .append("FROM card c ")
                .append("LEFT JOIN wish_card wc ")
                .append("  ON wc.card_id = c.id ")
                .append(" AND wc.member_id = :memberId ")
                .append(" AND wc.wish_flag = b'1' ")
                .append("WHERE 1=1 ");
        if (cond.getCategoryMajorId() != null)
            fb.append(WHERE_MAJOR_ID);
        if (cond.getCategoryMediumId() != null)
            fb.append(WHERE_MEDIUM_ID);
        if (hasName)
            fb.append(WHERE_LIKE_NAME);
        if (hasDesc)
            fb.append(WHERE_LIKE_DESC);
        fb.append(" ORDER BY c.id DESC LIMIT :limit OFFSET :offset ");

        Query qfb = em.createNativeQuery(fb.toString());
        if (cond.getCategoryMajorId() != null)
            qfb.setParameter(PARAM_MAJOR_ID, cond.getCategoryMajorId());
        if (cond.getCategoryMediumId() != null)
            qfb.setParameter(PARAM_MEDIUM_ID, cond.getCategoryMediumId());
        if (hasName)
            qfb.setParameter(PARAM_LIKE_NAME, "%" + rawName + "%");
        if (hasDesc)
            qfb.setParameter(PARAM_LIKE_DESC, "%" + rawDesc + "%");
        qfb.setParameter(PARAM_LIMIT, pageable.getPageSize());
        qfb.setParameter(PARAM_OFFSET, (int) pageable.getOffset());
        qfb.setParameter(PARAM_MEMBER_ID, memberParam);

        @SuppressWarnings("unchecked")
        List<Object[]> fbRows = qfb.getResultList();
        return fbRows;
    }

    private List<CardListRow> mapRowsToCardList(List<Object[]> rows) {
        List<CardListRow> content = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Long id = ((Number) r[0]).longValue();
            String name = (String) r[1];
            String code = (String) r[2];
            // NULL/이상값 방어: attribute
            CardAttribute attribute = null;
            String attrStr = (String) r[3];
            if (attrStr != null && !attrStr.isBlank()) {
                try {
                    attribute = CardAttribute.valueOf(attrStr);
                } catch (IllegalArgumentException ex) {
                    log.warn("Unknown CardAttribute value from DB: '{}' , cardId={}", attrStr, id);
                    attribute = null; // 또는 CardAttribute.UNKNOWN 를 쓰려면 enum에 상수 추가
                }
            }
            // NULL/이상값 방어: rarity
            Rarity rarity = null;
            String rarityStr = (String) r[4];
            if (rarityStr != null && !rarityStr.isBlank()) {
                try {
                    rarity = Rarity.valueOf(rarityStr);
                } catch (IllegalArgumentException ex) {
                    log.warn("Unknown Rarity value from DB: '{}' , cardId={}", rarityStr, id);
                    rarity = null; // 또는 Rarity.UNKNOWN 상수 추가
                }
            }
            Double score = (r[5] == null) ? 0.0 : ((Number) r[5]).doubleValue();
            boolean wished = ((Number) r[6]).intValue() == 1;

            content.add(new CardListRow(id, name, code, attribute, rarity, score, wished));
        }
        return content;
    }

    @Override
    public CardDetailResponse findDetailById(Long id) {
        final QCard card = QCard.card;
        return queryFactory
                .select(Projections.fields(CardDetailResponse.class,
                        card.id.as("id"),
                        card.categoryMajor.id.as("categoryMajorId"),
                        card.categoryMedium.id.as("categoryMediumId"),
                        card.code.as("code"),
                        card.name.as("name"),
                        card.description.as("description"),
                        card.attribute.stringValue().as("attribute"),
                        card.rarity.stringValue().as("rarity")))
                .from(card)
                .where(card.id.eq(id))
                .fetchOne(); // 없으면 null
    }
}
