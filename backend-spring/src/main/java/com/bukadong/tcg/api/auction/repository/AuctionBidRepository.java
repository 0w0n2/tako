package com.bukadong.tcg.api.auction.repository;

import com.bukadong.tcg.api.auction.entity.AuctionBid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 입찰 리포지토리
 * <P>
 * 입찰 생성/조회 담당.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Repository
public interface AuctionBidRepository extends JpaRepository<AuctionBid, Long> {
    boolean existsByEventId(String eventId);

}
