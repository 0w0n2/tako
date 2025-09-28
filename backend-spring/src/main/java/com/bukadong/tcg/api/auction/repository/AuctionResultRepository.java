package com.bukadong.tcg.api.auction.repository;

import com.bukadong.tcg.api.auction.entity.AuctionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 경매 결과 리포지토리
 * <P>
 * auctionId 유니크 제약으로 멱등 저장 보장.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public interface AuctionResultRepository extends JpaRepository<AuctionResult, Long>, AuctionResultRepositoryCustom {
    Optional<AuctionResult> findByAuctionId(Long auctionId);

    boolean existsByAuction_Id(Long auctionId);

    Optional<AuctionResult> findBySettleTxHash(String escrowContractAddress);
}
