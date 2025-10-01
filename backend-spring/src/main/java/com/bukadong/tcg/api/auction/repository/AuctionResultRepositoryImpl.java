package com.bukadong.tcg.api.auction.repository;

import static com.bukadong.tcg.api.auction.entity.QAuctionResult.auctionResult;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuctionResultRepositoryImpl implements AuctionResultRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * auctionId 로 auction_result 의 에스크로 컨트랙트 주소를 조회
     *
     * @param auctionId
     * @return
     */
    @Override
    public String findSettleTxHash(Long auctionId) {
        return queryFactory.select(auctionResult.settleTxHash)
                .from(auctionResult)
                .where(auctionResult.auction.id.eq(auctionId))
                .fetchFirst();
    }
}
