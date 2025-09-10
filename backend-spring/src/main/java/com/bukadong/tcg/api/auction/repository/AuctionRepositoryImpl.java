package com.bukadong.tcg.api.auction.repository;

import com.bukadong.tcg.api.auction.dto.response.AuctionListRow;
import com.bukadong.tcg.api.auction.entity.AuctionBidStatus;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.SubQueryExpression;
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
 * 동적 필터/정렬/페이지네이션 및 서브쿼리(bidCount/대표이미지)를 처리한다.
 * </P>
 */
@Repository
@RequiredArgsConstructor
public class AuctionRepositoryImpl implements AuctionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * @param categoryMajorId  카테고리 대분류 ID
     * @param categoryMediumId 카테고리 중분류 ID
     * @param titlePart        타이틀 부분검색어
     * @param cardId           카드 ID
     * @param currentPriceMin  현재가 최소
     * @param currentPriceMax  현재가 최대
     * @param grades           등급 집합
     * @param sort             정렬 기준
     * @param pageable         페이지 정보
     * @return 내부 행 DTO 페이지
     */

    @Override
    public Page<AuctionListRow> searchAuctions(Long categoryMajorId, Long categoryMediumId,
            String titlePart, Long cardId, BigDecimal currentPriceMin, BigDecimal currentPriceMax,
            Set<String> grades, AuctionSort sort, Pageable pageable) {

        BooleanBuilder where = new BooleanBuilder();
        if (categoryMajorId != null)
            where.and(auction.categoryMajor.id.eq(categoryMajorId));
        if (categoryMediumId != null)
            where.and(auction.categoryMedium.id.eq(categoryMediumId));
        if (titlePart != null && !titlePart.isBlank())
            where.and(auction.title.containsIgnoreCase(titlePart));
        if (cardId != null)
            where.and(auction.card.id.eq(cardId));
        if (currentPriceMin != null && currentPriceMax != null) {
            where.and(auction.currentPrice.between(currentPriceMin, currentPriceMax));
        } else if (currentPriceMin != null) {
            where.and(auction.currentPrice.goe(currentPriceMin));
        } else if (currentPriceMax != null) {
            where.and(auction.currentPrice.loe(currentPriceMax));
        }
        if (grades != null && !grades.isEmpty())
            where.and(auction.grade.in(grades));

        var bidCountSub = JPAExpressions.select(auctionBid.count()).from(auctionBid).where(
                auctionBid.auction.eq(auction).and(auctionBid.status.eq(AuctionBidStatus.VALID)));

        NumberExpression<Long> bidCountExpr = Expressions.numberTemplate(Long.class,
                "coalesce({0}, {1})", bidCountSub, 0L);

        OrderSpecifier<?> orderSpecifier = switch (sort) {
        case ENDTIME_ASC -> auction.endDatetime.asc();
        case ENDTIME_DESC -> auction.endDatetime.desc();
        case BIDCOUNT_DESC -> new OrderSpecifier<>(Order.DESC, bidCountExpr);
        case BIDCOUNT_ASC -> new OrderSpecifier<>(Order.ASC, bidCountExpr);
        };

        // 서브쿼리를 사용하면 경매 행마다 실행되어 성능 저하와 'Subquery returns more than 1 row' 오류가 발생할 수 있어
        // JOIN 방식으로 단일 대표이미지를 가져오도록 함
        List<AuctionListRow> content = queryFactory
                .select(Projections.constructor(AuctionListRow.class, auction.id, auction.grade,
                        auction.title, auction.currentPrice, bidCountExpr, auction.endDatetime,
                        media.url // 대표이미지
                )).from(auction).leftJoin(media)
                .on(media.ownerId.eq(auction.id).and(media.type.eq(MediaType.AUCTION_ITEM))
                        .and(media.seqNo.eq(1))) // ← seq_no = 1로 고정 대표이미지
                .where(where).orderBy(orderSpecifier, auction.id.desc())
                .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        Long total = queryFactory.select(auction.count()).from(auction).where(where).fetchOne();
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

}
