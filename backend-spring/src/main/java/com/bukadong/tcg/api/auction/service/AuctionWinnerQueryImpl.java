package com.bukadong.tcg.api.auction.service;

import static com.bukadong.tcg.api.bid.entity.QAuctionBid.auctionBid;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bukadong.tcg.api.auction.service.dto.WinnerSnapshot;
import com.bukadong.tcg.api.bid.entity.AuctionBidStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/**
 * 낙찰자 조회 구현체
 * <P>
 * 최고가 유효 입찰 1건을 스냅샷으로 반환한다. 없으면 Optional.empty().
 * </P>
 * 
 * @PARAM auctionId 경매 ID
 * @RETURN Optional<WinnerSnapshot>
 */
@Service
@RequiredArgsConstructor
public class AuctionWinnerQueryImpl implements AuctionWinnerQuery {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<WinnerSnapshot> tryGetWinnerSnapshot(Long auctionId) {
        return queryFactory
                .select(Projections.constructor(WinnerSnapshot.class, auctionBid.id, auctionBid.member.id,
                        auctionBid.amount, auctionBid.createdAt))
                .from(auctionBid)
                .where(auctionBid.auction.id.eq(auctionId), auctionBid.status.eq(AuctionBidStatus.VALID))
                .orderBy(auctionBid.amount.desc(), auctionBid.id.asc()).limit(1).fetch().stream().findFirst();
    }
}
