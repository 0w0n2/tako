package com.bukadong.tcg.api.auction.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bukadong.tcg.api.auction.entity.AuctionReview;

public interface AuctionReviewRepository extends JpaRepository<AuctionReview, Long> {

    /**
     * 특정 회원이 받은 후기 조회
     * 
     * @PARAM memberId 회원 ID
     * @RETURN 후기 목록
     */
    List<AuctionReview> findByAuctionMemberId(Long memberId);

    Optional<AuctionReview> findByMember_IdAndAuction_Id(Long memberId, Long auctionId);
}
