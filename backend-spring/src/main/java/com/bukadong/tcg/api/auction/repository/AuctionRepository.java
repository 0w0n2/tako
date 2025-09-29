package com.bukadong.tcg.api.auction.repository;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionCloseReason;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 경매 리포지토리
 * <P>
 * 경매에서 카드 식별자를 투영 조회한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 표준 CRUD + 커스텀 조회
 */
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    /** Delivery ID로 역참조하여 경매 조회 */
    @Query("select a from Auction a where a.delivery.id = :deliveryId")
    Optional<Auction> findByDeliveryId(@Param("deliveryId") Long deliveryId);

    /**
     * 내 경매 목록
     */
    org.springframework.data.domain.Page<Auction> findByMember_IdOrderByIdDesc(Long memberId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * 경매 ID로 연결된 카드 ID를 조회한다.
     * <P>
     * 엔티티 전체 로딩 대신 JPQL로 단일 컬럼 투영.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @RETURN 카드 ID Optional
     */
    @Query("select a.card.id from Auction a where a.id = :auctionId")
    Optional<Long> findCardIdByAuctionId(@Param("auctionId") Long auctionId);

    @Query("select a.categoryMajor.id from Auction a where a.id = :auctionId")
    Optional<Long> findCategoryMajorIdByAuctionId(@Param("auctionId") Long auctionId);

    /**
     * 상세 조회용 fetch join 대체(@EntityGraph)
     * <P>
     * 주의: 복잡한 join은 QueryDSL 커스텀으로 처리.
     * </P>
     * 
     * @PARAM id 경매 ID
     * @RETURN Optional<Auction>
     */
    @EntityGraph(attributePaths = { "card", "categoryMajor", "categoryMedium" })
    @Query("select a from Auction a where a.id = :id")
    Optional<Auction> findByIdWithCardAndCategory(@Param("id") Long id);

    /**
     * 카테고리 참조 여부 확인용
     * <P>
     * 카테고리 대분류 ID
     * </P>
     * 
     * @param majorId
     * @return
     */
    @Query("select count(a) from Auction a where a.categoryMajor.id = :majorId")
    long countByCategoryMajorId(@Param("majorId") Long majorId);

    /**
     * 카테고리 참조 여부 확인용
     * <P>
     * 카테고리 중분류 ID
     * </P>
     * 
     * @param mediumId
     * @return
     */
    @Query("select count(a) from Auction a where a.categoryMedium.id = :mediumId")
    long countByCategoryMediumId(@Param("mediumId") Long mediumId);

    // 추가
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
              update Auction a
                 set a.isEnd = true,
                     a.closeReason = :reason,
                     a.closedAt = :closedAt,
                     a.updatedAt = :closedAt
               where a.id = :id
                 and a.isEnd = false
            """)
    int closeManually(@Param("id") Long id, @Param("reason") AuctionCloseReason reason,
            @Param("closedAt") LocalDateTime closedAt);

    /**
     * 마감 도래했고 아직 종료되지 않은 경매를 종료 상태로 전환
     * <P>
     * isEnd=false && endDatetime<=NOW인 경우에만 종료 마킹.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @PARAM reason 종료 사유
     * @RETURN 업데이트된 행 수(0 또는 1)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
              update Auction a
                 set a.isEnd = true,
                     a.closeReason = :reason,
                     a.closedAt = :nowUtc,
                     a.updatedAt = :nowUtc
               where a.id = :auctionId
                 and a.isEnd = false
                 and a.endDatetime <= :nowUtc
            """)
    int closeIfDue(@Param("auctionId") long auctionId, @Param("reason") AuctionCloseReason reason,
            @Param("nowUtc") LocalDateTime nowUtc);

    /**
     * 다건 종료(배치)
     * <P>
     * ID 목록 중 마감 도래 + 미종료만 종료 처리.
     * </P>
     * 
     * @PARAM auctionIds 경매 ID 목록
     * @PARAM reason 종료 사유
     * @RETURN 업데이트된 행 수
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
              update Auction a
                 set a.isEnd = true,
                     a.closeReason = :reason,
                     a.closedAt = :nowUtc
               where a.id in :auctionIds
                 and a.isEnd = false
                 and a.endDatetime <= :nowUtc
            """)
    int closeIfDueIn(@Param("auctionIds") List<Long> auctionIds, @Param("reason") AuctionCloseReason reason,
            @Param("nowUtc") LocalDateTime nowUtc);

    /**
     * 부트스트랩: 미종료 & end_datetime <= horizon 인 경매의 (id, endAtMillis) 목록 (MySQL:
     * unix_timestamp*1000)
     * <P>
     * DB 컬럼명에 맞춰 수정: status/end_at → is_end/end_datetime
     * </P>
     */
    @Query(value = """
            select a.id as auction_id,
                   cast(unix_timestamp(a.end_datetime) * 1000 as signed) as endAtMillis
              from auction a
             where a.is_end = 0
               and a.end_datetime <= :horizon
            """, nativeQuery = true)
    List<Object[]> findOpenEndAtBefore(@Param("horizon") Instant horizon);

    /**
     * 종료 처리용 락 조회
     * <P>
     * SELECT ... FOR UPDATE
     * </P>
     * 
     * @PARAM id 경매 ID
     * @RETURN Optional<Auction>
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Auction a where a.id = :id")
    Optional<Auction> findByIdForUpdate(@Param("id") Long id);

    /**
     * 회원이 입찰한 진행중 경매 목록 조회 (페이지네이션)
     */
    @Query("""
            select a from Auction a
             where a.isEnd = false
               and exists (select 1 from AuctionBid b where b.auction = a and b.member.id = :memberId)
             order by a.endDatetime asc, a.id asc
            """)
    org.springframework.data.domain.Page<Auction> findOngoingByMemberBids(@Param("memberId") Long memberId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * 회원이 입찰한 종료된 경매 목록 조회 (페이지네이션)
     * <p>
     * 종료된 경매는 최근 종료된 항목이 먼저 오도록 endDatetime DESC 정렬. (요구사항: "종료된 경매도 끝나는 시간 순" 해석을
     * 최근 종료 우선으로 가정. 필요 시 ASC로 변경 가능.)
     * </p>
     */
    @Query("""
            select a from Auction a
             where a.isEnd = true
               and exists (select 1 from AuctionBid b where b.auction = a and b.member.id = :memberId)
             order by a.endDatetime desc, a.id desc
            """)
    org.springframework.data.domain.Page<Auction> findEndedByMemberBids(@Param("memberId") Long memberId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * 미종료 경매 ID 전체 조회(부팅 워밍용)
     * <P>
     * closeReason이 NULL이거나 isEnd=false 인 것 기준. 도메인 필드 기준으로 isEnd=false를 우선 사용.
     * </P>
     */
    @Query("select a.id from Auction a where a.isEnd = false")
    List<Long> findAllOpenIds();

    /**
     * 구매자 기준: 내가 리뷰를 아직 작성하지 않은(대상) 경매 목록 조건: winnerMemberId = :buyerId,
     * delivery.status = CONFIRMED, 해당 경매/회원 조합의 리뷰가 없음
     */
    @Query("""
            select a from Auction a
             left join a.delivery d
            where a.winnerMemberId = :buyerId
                    and d.status = com.bukadong.tcg.api.delivery.entity.DeliveryStatus.CONFIRMED
                    and not exists (select 1 from AuctionReview r where r.auction = a and r.member.id = :buyerId)
            order by a.endDatetime desc, a.id desc
            """)
    List<Auction> findBuyerAwaitingReview(@Param("buyerId") Long buyerId);

    /**
     * 구매자 기준: 내가 작성 완료한 리뷰 포함 경매 목록 (경매 + 리뷰 함께 반환)
     */
    @Query("""
            select a, r from Auction a
             join a.delivery d
             join AuctionReview r on r.auction = a and r.member.id = :buyerId
            where a.winnerMemberId = :buyerId
                    and d.status = com.bukadong.tcg.api.delivery.entity.DeliveryStatus.CONFIRMED
            order by r.createdAt desc, a.id desc
            """)
    List<Object[]> findBuyerDoneWithReview(@Param("buyerId") Long buyerId);

    /**
     * 판매자 기준: 내가 리뷰를 받을 예정인 경매 목록 (아직 구매자 리뷰 없음)
     */
    @Query("""
            select a from Auction a
             join a.delivery d
            where a.member.id = :sellerId
                    and a.winnerMemberId is not null
                    and d.status = com.bukadong.tcg.api.delivery.entity.DeliveryStatus.CONFIRMED
                    and not exists (select 1 from AuctionReview r where r.auction = a and r.member.id = a.winnerMemberId)
            order by a.endDatetime desc, a.id desc
            """)
    List<Auction> findSellerAwaitingReview(@Param("sellerId") Long sellerId);

    /**
     * 판매자 기준: 내가 받은 리뷰 포함 경매 목록 (경매 + 리뷰 함께 반환)
     */
    @Query("""
            select a, r from Auction a
             join a.delivery d
             join AuctionReview r on r.auction = a and r.member.id = a.winnerMemberId
            where a.member.id = :sellerId
                    and d.status = com.bukadong.tcg.api.delivery.entity.DeliveryStatus.CONFIRMED
            order by r.createdAt desc, a.id desc
            """)
    List<Object[]> findSellerDoneWithReview(@Param("sellerId") Long sellerId);
}