package com.bukadong.tcg.api.auction.repository;

import com.bukadong.tcg.api.auction.dto.response.AuctionDetailResponse.*;
import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.media.entity.MediaKind;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.bukadong.tcg.api.auction.entity.QAuction.auction;
import static com.bukadong.tcg.api.auction.entity.QAuctionBid.auctionBid;
import static com.bukadong.tcg.api.media.entity.QMedia.media;
import static com.bukadong.tcg.api.member.entity.QMember.member;

/**
 * 경매 상세 전용 커스텀 레포지토리 구현
 * <P>
 * QueryDSL + 일부분 Native(집계) 혼용. 서버 타임존: Asia/Seoul.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN DTO 매핑 결과
 */
@Repository
@RequiredArgsConstructor
public class AuctionDetailRepositoryImpl implements AuctionDetailRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public AuctionInfo mapAuctionInfo(Auction a) {
        return AuctionInfo.builder().id(a.getId()).title(a.getTitle()).detail(a.getDetail())
                .grade(a.getGrade()).code(a.getCode()).startPrice(a.getStartPrice())
                .currentPrice(a.getCurrentPrice())
                .bidUnit(a.getBidUnit() != null ? new BigDecimal(a.getBidUnit().value()) : null)
                .startDatetime(a.getStartDatetime()).endDatetime(a.getEndDatetime())
                .durationDays(a.getDurationDays()).end(a.isEnd()).buyNowFlag(a.isBuyNowFlag())
                .buyNowPrice(a.getBuyNowPrice()).extensionFlag(a.isExtensionFlag())
                .createdAt(a.getCreatedAt()).build();
    }

    @Override
    public CardInfo mapCardInfo(Auction a) {
        return CardInfo.builder().categoryMajorId(a.getCategoryMajor().getId())
                .categoryMajorName(a.getCategoryMajor().getName())
                .categoryMediumId(a.getCategoryMedium().getId())
                .categoryMediumName(a.getCategoryMedium().getName()).cardName(a.getCard().getName())
                .cardDescription(a.getCard().getDescription())
                .attribute(a.getCard().getAttribute() != null ? a.getCard().getAttribute().name()
                        : null)
                .rarity(a.getCard().getRarity().name()).build();
    }

    @Override
    public List<String> findImageUrlsByAuctionId(Long auctionId) {
        return queryFactory.select(media.url).from(media)
                .where(media.type.eq(MediaType.AUCTION_ITEM).and(media.ownerId.eq(auctionId))
                        .and(media.mediaKind.eq(MediaKind.IMAGE)))
                .orderBy(media.seqNo.asc(), media.id.asc()).fetch();
    }

    @Override
    public List<DailyPriceLine> findWeeklyPriceLinesByCardId(Long cardId) {
        // 서버 표준 타임존(Asia/Seoul) 기준 최근 7일
        ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime from = nowKst.minusDays(6).toLocalDate()
                .atStartOfDay(ZoneId.of("Asia/Seoul"));
        // Native SQL 사용 (auction_result + auction_bid + auction + card)
        // 가정: auction_result (auction_bid_id, created_at), auction_bid (id, bid_price,
        // auction_id), auction (id, card_id)
        // 일자별 집계: DATE(CONVERT_TZ(ar.created_at, 'UTC', 'Asia/Seoul')) 기준 또는 DB타임존이
        // KST면 DATE(ar.created_at)
        String sql = "SELECT DATE(ar.created_at) AS d, " + "       MIN(ab.bid_price) AS min_price, "
                + "       MAX(ab.bid_price) AS max_price, "
                + "       AVG(ab.bid_price) AS avg_price " + "  FROM auction_result ar "
                + "  JOIN auction_bid ab ON ab.id = ar.auction_bid_id "
                + "  JOIN auction a ON a.id = ab.auction_id " + " WHERE a.card_id = :cardId "
                + "   AND ar.created_at >= :fromDate " + " GROUP BY DATE(ar.created_at) "
                + " ORDER BY DATE(ar.created_at) ASC";
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql).setParameter("cardId", cardId)
                .setParameter("fromDate", java.sql.Timestamp.valueOf(from.toLocalDateTime()))
                .getResultList();

        return rows.stream()
                .map(r -> DailyPriceLine.builder().date(((java.sql.Date) r[0]).toLocalDate())
                        .minPrice((BigDecimal) r[1]).maxPrice((BigDecimal) r[2])
                        .avgPrice((BigDecimal) r[3]).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<BidHistoryItem> findBidHistory(Long auctionId, int limit) {
        // 최근순으로 가져오고 응답은 최신→과거 (UI 역정렬 가능)
        List<Tuple> tuples = queryFactory
                .select(auctionBid.createdAt, auctionBid.bidPrice, member.nickname).from(auctionBid)
                .join(auctionBid.member, member).where(auctionBid.auction.id.eq(auctionId))
                .orderBy(auctionBid.createdAt.desc()).limit(limit).fetch();

        return tuples.stream()
                .map(t -> BidHistoryItem.builder().createdAt(t.get(auctionBid.createdAt))
                        .bidPrice(t.get(auctionBid.bidPrice))
                        .bidderNickname(t.get(member.nickname))
                        .build())
                .collect(Collectors.toList());
    }
}
