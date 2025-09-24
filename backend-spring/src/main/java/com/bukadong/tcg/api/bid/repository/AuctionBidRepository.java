package com.bukadong.tcg.api.bid.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.bukadong.tcg.api.bid.entity.AuctionBid;

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
    // eventId로 입찰 존재 여부 확인
    boolean existsByEventId(String eventId);

    // 경매에 입찰이 1건이라도 있는지 확인
    boolean existsByAuction_Id(Long auctionId);

    // 경매별 최고 입찰 1건 조회 (낙찰자 선정용)
    Optional<AuctionBid> findTopByAuctionIdOrderByAmountDescCreatedAtDesc(Long auctionId);

    // 경매별 입찰 내역 최신순 (Pageable로 개수 제한)
    java.util.List<AuctionBid> findByAuction_IdOrderByCreatedAtDesc(Long auctionId, Pageable pageable);

    // 특정 경매에서 특정 회원의 최고 입찰(금액 내림차순, 동일금액이면 최신순) 1건
    java.util.Optional<AuctionBid> findTopByAuction_IdAndMember_IdOrderByAmountDescCreatedAtDesc(Long auctionId,
            Long memberId);
}
