package com.bukadong.tcg.api.wish.repository;

import java.util.List;

/**
 * 위시 유저 조회 포트
 * <P>
 * 카드/경매별 위시 사용자 ID 리스트를 조회한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public interface WishQueryPort {
    List<Long> findMemberIdsWhoWishedCard(Long cardId);

    List<Long> findMemberIdsWhoWishedAuction(Long auctionId);
}
