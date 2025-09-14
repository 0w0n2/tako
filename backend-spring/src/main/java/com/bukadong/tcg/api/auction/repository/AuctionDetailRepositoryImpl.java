package com.bukadong.tcg.api.auction.repository;

import com.bukadong.tcg.api.auction.dto.response.AuctionDetailResponse.*;
import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaUrlService;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.bukadong.tcg.api.auction.entity.QAuction.auction;
import static com.bukadong.tcg.api.auction.entity.QAuctionBid.auctionBid;
import static com.bukadong.tcg.api.member.entity.QMember.member;
import static com.bukadong.tcg.api.auction.entity.QAuctionReview.auctionReview;

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
    private final MediaUrlService mediaUrlService;

    @Override
    public AuctionInfo mapAuctionInfo(Auction a) {
        return AuctionInfo.builder().id(a.getId()).title(a.getTitle()).detail(a.getDetail()).grade(a.getGrade())
                .code(a.getCode()).startPrice(a.getStartPrice()).currentPrice(a.getCurrentPrice())
                .bidUnit(a.getBidUnit() != null ? new BigDecimal(a.getBidUnit().value()) : null)
                .startDatetime(a.getStartDatetime()).endDatetime(a.getEndDatetime()).end(a.isEnd())
                .buyNowFlag(a.isBuyNowFlag()).buyNowPrice(a.getBuyNowPrice()).extensionFlag(a.isExtensionFlag())
                .createdAt(a.getCreatedAt()).build();
    }

    @Override
    public CardInfo mapCardInfo(Auction a) {
        return CardInfo.builder().categoryMajorId(a.getCategoryMajor().getId())
                .categoryMajorName(a.getCategoryMajor().getName()).categoryMediumId(a.getCategoryMedium().getId())
                .categoryMediumName(a.getCategoryMedium().getName()).cardName(a.getCard().getName())
                .cardDescription(a.getCard().getDescription())
                .attribute(a.getCard().getAttribute() != null ? a.getCard().getAttribute().name() : null)
                .rarity(a.getCard().getRarity().name()).build();
    }

    @Override
    public List<DailyPriceLine> findWeeklyPriceLinesByCardId(Long cardId) {
        ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime from = nowKst.minusDays(6).toLocalDate().atStartOfDay(ZoneId.of("Asia/Seoul"));

        String sql = "SELECT DATE(ar.created_at) AS d, " + "       MIN(ab.bid_price) AS min_price, "
                + "       MAX(ab.bid_price) AS max_price, " + "       AVG(ab.bid_price) AS avg_price "
                + "  FROM auction_result ar " + "  JOIN auction_bid ab ON ab.id = ar.auction_bid_id "
                + "  JOIN auction a ON a.id = ab.auction_id " + " WHERE a.card_id = :cardId "
                + "   AND ar.created_at >= :fromDate " + " GROUP BY DATE(ar.created_at) "
                + " ORDER BY DATE(ar.created_at) ASC";

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql).setParameter("cardId", cardId)
                .setParameter("fromDate", java.sql.Timestamp.valueOf(from.toLocalDateTime())).getResultList();

        return rows.stream()
                .map(r -> DailyPriceLine.builder().date(((java.sql.Date) r[0]).toLocalDate())
                        .minPrice((BigDecimal) r[1]).maxPrice((BigDecimal) r[2]).avgPrice((BigDecimal) r[3]).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<BidHistoryItem> findBidHistory(Long auctionId, int limit) {
        List<Tuple> tuples = queryFactory.select(auctionBid.createdAt, auctionBid.bidPrice, member.nickname)
                .from(auctionBid).join(auctionBid.member, member).where(auctionBid.auction.id.eq(auctionId))
                .orderBy(auctionBid.createdAt.desc()).limit(limit).fetch();

        return tuples.stream()
                .map(t -> BidHistoryItem.builder().createdAt(t.get(auctionBid.createdAt))
                        .bidPrice(t.get(auctionBid.bidPrice)).bidderNickname(t.get(member.nickname)).build())
                .collect(Collectors.toList());
    }

    /**
     * 판매자 정보 조회 구현
     * <P>
     * 1) 경매 → 판매자(member) id, nickname 조회<br>
     * 2) 판매자가 소유한 모든 경매에 달린 후기(auction_review)를 집계해 리뷰 수/평균 별점 계산<br>
     * 3) 판매자의 프로필 이미지(MediaType.MEMBER_PROFILE, seq_no=1) 조회
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @RETURN SellerInfo
     */
    @Override
    public SellerInfo findSellerInfoByAuctionId(Long auctionId) {
        // 1) 경매의 판매자 식별
        Tuple seller = queryFactory.select(member.id, member.nickname).from(auction).join(auction.member, member)
                .where(auction.id.eq(auctionId)).fetchOne();

        if (seller == null) {
            return SellerInfo.builder().id(null).nickname(null).reviewCount(0L).reviewStarAvg(null)
                    .profileImageUrl(null).build();
        }

        Long sellerId = seller.get(member.id);
        String nickname = seller.get(member.nickname);

        // 2) 리뷰 수/평균 별점 집계
        Tuple agg = queryFactory.select(auctionReview.id.count(), auctionReview.star.avg()).from(auctionReview)
                .join(auction).on(auctionReview.auction.id.eq(auction.id)).where(auction.member.id.eq(sellerId))
                .fetchOne();

        long reviewCount = (agg != null && agg.get(auctionReview.id.count()) != null)
                ? agg.get(auctionReview.id.count())
                : 0L;
        Double starAvg = (agg != null) ? agg.get(auctionReview.star.avg()) : null;

        // 3) 프로필 이미지 조회 (대표 이미지만 1개)
        String profileImageUrl = mediaUrlService
                .getPresignedImageUrls(MediaType.MEMBER_PROFILE, sellerId, Duration.ofMinutes(5))
                .stream().findFirst().orElse(null);
                
        return SellerInfo.builder().id(sellerId).nickname(nickname).reviewCount(reviewCount).reviewStarAvg(starAvg)
                .profileImageUrl(profileImageUrl).build();
    }

}
