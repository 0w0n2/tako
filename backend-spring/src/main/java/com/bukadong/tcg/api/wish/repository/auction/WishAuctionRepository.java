package com.bukadong.tcg.api.wish.repository.auction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bukadong.tcg.api.wish.entity.WishAuction;

import java.util.List;
import java.util.Optional;

/**
 * WishAuction JPA 리포지토리
 * <P>
 * memberId+auctionId 단건 조회 및 upsert 용도.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public interface WishAuctionRepository extends JpaRepository<WishAuction, Long>, WishAuctionRepositoryCustom {
    Optional<WishAuction> findByMemberIdAndAuctionId(Long memberId, Long auctionId);

    boolean existsByMemberIdAndAuctionIdAndWishFlagTrue(Long memberId, Long auctionId);

    @Query("select wa.auctionId from WishAuction wa "
            + "where wa.memberId = :memberId and wa.wishFlag = true and wa.auctionId in :auctionIds")
    List<Long> findWishedAuctionIds(@Param("memberId") Long memberId, @Param("auctionIds") List<Long> auctionIds);
}
