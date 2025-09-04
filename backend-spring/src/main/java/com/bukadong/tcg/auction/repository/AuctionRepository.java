package com.bukadong.tcg.auction.repository;

import com.bukadong.tcg.auction.entity.Auction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

/**
 * 경매 엔티티({@link Auction})용 Spring Data JPA 레포지토리.
 *
 * <p>
 * 기본 CRUD 외에도 경매 상태(진행중, 예정, 종료) 및 카드 기준으로 검색하는 메서드를 제공한다.
 * </p>
 */
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    /**
     * 현재 진행중인 경매 목록 조회.
     *
     * <p>
     * {@code startDatetime < now < endDatetime} 조건을 만족하는 경매를 반환한다.
     * </p>
     *
     * @param now1     현재 시각 (startDatetime 비교용, 일반적으로 {@code LocalDateTime.now()})
     * @param now2     현재 시각 (endDatetime 비교용, 일반적으로 {@code LocalDateTime.now()})
     * @param pageable 페이지네이션 정보
     * @return 진행중인 경매 페이지 결과
     */
    Page<Auction> findByStartDatetimeBeforeAndEndDatetimeAfter(
            LocalDateTime now1,
            LocalDateTime now2,
            Pageable pageable);

    /**
     * 예정된 경매 목록 조회.
     *
     * <p>
     * {@code startDatetime > now} 조건을 만족하는 경매를 반환한다.
     * </p>
     *
     * @param now      현재 시각
     * @param pageable 페이지네이션 정보
     * @return 예정된 경매 페이지 결과
     */
    Page<Auction> findByStartDatetimeAfter(LocalDateTime now, Pageable pageable);

    /**
     * 종료된 경매 목록 조회.
     *
     * <p>
     * {@code endDatetime < now} 조건을 만족하는 경매를 반환한다.
     * </p>
     *
     * @param now      현재 시각
     * @param pageable 페이지네이션 정보
     * @return 종료된 경매 페이지 결과
     */
    Page<Auction> findByEndDatetimeBefore(LocalDateTime now, Pageable pageable);

    /**
     * 특정 카드 기준으로 경매 목록 조회.
     *
     * <p>
     * 실물카드(PhysicalCard)가 참조하는 카드(Card)의 ID를 기준으로 경매를 검색한다.
     * </p>
     *
     * @param cardId   카드 ID
     * @param pageable 페이지네이션 정보
     * @return 해당 카드와 연관된 경매 페이지 결과
     */
    Page<Auction> findByPhysicalCard_Card_Id(Long cardId, Pageable pageable);
}
