package com.bukadong.tcg.api.auction.repository;

import static com.bukadong.tcg.api.auction.entity.QAuction.auction;
import static com.bukadong.tcg.api.auction.entity.QAuctionResult.auctionResult;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.bukadong.tcg.api.auction.dto.response.CardAuctionHistoryItemResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CardAuctionHistoryRepositoryImpl implements CardAuctionHistoryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CardAuctionHistoryItemResponse> findCompletedHistoriesByCardId(Long cardId, int fromDays) {
        LocalDateTime from = LocalDateTime.now().minusDays(fromDays);

        return queryFactory
                .select(Projections.constructor(CardAuctionHistoryItemResponse.class, auctionResult.createdAt, // successfulBidTime
                        auction.grade.gradeCode, // grade
                        auctionResult.auctionBid.amount // amount
                )).from(auctionResult).join(auctionResult.auction, auction)
                .where(auction.card.id.eq(cardId).and(auction.isEnd.isTrue()).and(auctionResult.createdAt.goe(from)))
                .orderBy(auctionResult.createdAt.desc()).fetch();
    }
}
