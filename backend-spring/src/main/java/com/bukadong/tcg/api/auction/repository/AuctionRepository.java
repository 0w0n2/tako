package com.bukadong.tcg.api.auction.repository;

import com.bukadong.tcg.api.auction.entity.Auction;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE auction SET is_end = 1 WHERE id = :id AND is_end = 0", nativeQuery = true)
    int markEnded(@Param("id") Long id);

    /**
     * 마감 시간 경과한 OPEN 경매를 CLOSED로 전이(영향 행 1 = 유일 종료자)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update Auction a
                   set a.status = com.bukadong.tcg.api.auction.entity.AuctionStatus.CLOSED,
                       a.closedAt = CURRENT_TIMESTAMP
                 where a.id = :auctionId
                   and a.status = com.bukadong.tcg.api.auction.entity.AuctionStatus.OPEN
                   and a.endAt <= CURRENT_TIMESTAMP
            """)
    int closeIfDue(@Param("auctionId") long auctionId);

    /**
     * 부트스트랩: OPEN & endAt <= horizon 인 경매의 (id, endAtMillis) 목록 (MySQL:
     * unix_timestamp*1000)
     */
    @Query(value = """
                select a.id as auction_id,
                       cast(unix_timestamp(a.end_at) * 1000 as signed) as endAtMillis
                  from auction a
                 where a.status = 'OPEN'
                   and a.end_at <= :horizon
            """, nativeQuery = true)
    List<Object[]> findOpenEndAtBefore(@Param("horizon") Instant horizon);
}