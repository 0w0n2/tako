package com.bukadong.tcg.api.auction.repository;

import com.bukadong.tcg.api.auction.dto.projection.AuctionListProjection;
import com.bukadong.tcg.api.auction.entity.AuctionBidStatus;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import com.querydsl.core.types.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.bukadong.tcg.api.auction.entity.QAuction.auction;
import static com.bukadong.tcg.api.auction.entity.QAuctionBid.auctionBid;
import static com.bukadong.tcg.api.media.entity.QMedia.media;

/**
 * 경매 목록 QueryDSL 검색 리포지토리 구현
 * <P>
 * 동적 필터/정렬/페이지네이션을 처리한다. 성능을 위해 입찰수는 LEFT JOIN + GROUP BY로 집계하여 행당 서브쿼리를 제거한다.
 * </P>
 */
@Repository
@RequiredArgsConstructor
public class AuctionRepositoryImpl implements AuctionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 경매 목록 검색
     * <P>
     * 대표이미지는 (type=AUCTION_ITEM, seq_no=1)만 LEFT JOIN하여 1:1을 보장한다. 입찰수는 VALID 상태만
     * 조인 후 COUNT로 집계한다.
     * </P>
     * 
     * @PARAM categoryMajorId 카테고리 대분류 ID
     * @PARAM categoryMediumId 카테고리 중분류 ID
     * @PARAM titlePart 타이틀 부분검색어
     * @PARAM cardId 카드 ID
     * @PARAM currentPriceMin 현재가 최소
     * @PARAM currentPriceMax 현재가 최대
     * @PARAM grades 등급 집합
     * @PARAM sort 정렬 기준
     * @PARAM pageable 페이지 정보
     * @RETURN 내부 행 DTO 페이지
     */
    @Override
    public Page<AuctionListProjection> searchAuctions(Long categoryMajorId, Long categoryMediumId, String titlePart,
            Long cardId, BigDecimal currentPriceMin, BigDecimal currentPriceMax, Set<String> grades, AuctionSort sort,
            Pageable pageable) {

        BooleanBuilder where = buildWhere(categoryMajorId, categoryMediumId, titlePart, cardId, currentPriceMin,
                currentPriceMax, grades);

        // 입찰수: VALID만 LEFT JOIN 후 COUNT
        NumberExpression<Long> bidCountExpr = auctionBid.id.count();

        OrderSpecifier<?>[] orderSpecifiers = buildOrder(sort, bidCountExpr);

        // 콘텐츠 쿼리
        List<AuctionListProjection> content = queryFactory.select(Projections.constructor(AuctionListProjection.class,
                auction.id, auction.grade.gradeCode, auction.title, auction.currentPrice, bidCountExpr, // 집계된 입찰수
                auction.endDatetime, media.s3keyOrUrl // 대표 이미지 key (seq_no=1)
        )).from(auction).leftJoin(auctionBid)
                .on(auctionBid.auction.eq(auction).and(auctionBid.status.eq(AuctionBidStatus.VALID))).leftJoin(media)
                .on(media.ownerId.eq(auction.id).and(media.type.eq(MediaType.AUCTION_ITEM)).and(media.seqNo.eq(1)))
                .where(where)
                .groupBy(auction.id, auction.grade.gradeCode, auction.title, auction.currentPrice, auction.endDatetime,
                        media.s3keyOrUrl)
                .orderBy(orderSpecifiers).offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        // 카운트 쿼리(조인/그룹 없이 where만)
        Long total = queryFactory.select(auction.count()).from(auction).where(where).fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    /**
     * 동적 where 절 빌더
     */
    private BooleanBuilder buildWhere(Long categoryMajorId, Long categoryMediumId, String titlePart, Long cardId,
            BigDecimal currentPriceMin, BigDecimal currentPriceMax, Set<String> grades) {
        BooleanBuilder where = new BooleanBuilder();

        if (categoryMajorId != null) {
            where.and(auction.categoryMajor.id.eq(categoryMajorId));
        }
        if (categoryMediumId != null) {
            where.and(auction.categoryMedium.id.eq(categoryMediumId));
        }
        if (titlePart != null && !titlePart.isBlank()) {
            where.and(auction.title.containsIgnoreCase(titlePart));
        }
        if (cardId != null) {
            where.and(auction.card.id.eq(cardId));
        }
        if (currentPriceMin != null && currentPriceMax != null) {
            where.and(auction.currentPrice.between(currentPriceMin, currentPriceMax));
        } else if (currentPriceMin != null) {
            where.and(auction.currentPrice.goe(currentPriceMin));
        } else if (currentPriceMax != null) {
            where.and(auction.currentPrice.loe(currentPriceMax));
        }
        if (grades != null && !grades.isEmpty()) {
            where.and(auction.grade.gradeCode.in(grades));
        }
        return where;
    }

    /**
     * 정렬 스펙 빌더(타이브레이커: id DESC)
     */
    private OrderSpecifier<?>[] buildOrder(AuctionSort sort, NumberExpression<Long> bidCountExpr) {
        OrderSpecifier<?> tieBreaker = auction.id.desc();
        return switch (sort) {
        case ENDTIME_ASC -> new OrderSpecifier<?>[] { auction.endDatetime.asc(), tieBreaker };
        case ENDTIME_DESC -> new OrderSpecifier<?>[] { auction.endDatetime.desc(), tieBreaker };
        case BIDCOUNT_DESC -> new OrderSpecifier<?>[] { new OrderSpecifier<>(Order.DESC, bidCountExpr), tieBreaker };
        case BIDCOUNT_ASC -> new OrderSpecifier<?>[] { new OrderSpecifier<>(Order.ASC, bidCountExpr), tieBreaker };
        };
    }
}
