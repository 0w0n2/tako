package com.bukadong.tcg.api.wish.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 위시 유저 조회 JDBC 리포지터리
 * <P>
 * 스키마: wish_card, wish_auction. wish_flag=1인 사용자만 반환.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Repository
@RequiredArgsConstructor
public class JdbcWishQueryRepository implements WishQueryPort {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Long> findMemberIdsWhoWishedCard(Long cardId) {
        String sql = "SELECT member_id FROM wish_card WHERE card_id = ? AND wish_flag = 1";
        return jdbcTemplate.query(sql, (rs, i) -> rs.getLong(1), cardId);
    }

    @Override
    public List<Long> findMemberIdsWhoWishedAuction(Long auctionId) {
        String sql = "SELECT member_id FROM wish_auction WHERE auction_id = ? AND wish_flag = 1";
        return jdbcTemplate.query(sql, (rs, i) -> rs.getLong(1), auctionId);
    }
}
