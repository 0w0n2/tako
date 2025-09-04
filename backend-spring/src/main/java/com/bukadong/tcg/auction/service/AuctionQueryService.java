package com.bukadong.tcg.auction.service;

import com.bukadong.tcg.auction.entity.Auction;
import com.bukadong.tcg.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 경매 읽기 전용 조회 서비스.
 *
 * <p>
 * 비즈니스 로직을 단순화하여 컨트롤러에서
 * 경매 상태(진행중, 예정, 종료) 또는 카드 기준으로
 * 검색할 수 있도록 한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AuctionQueryService {

    /**
     * 경매 상태 구분.
     *
     * <ul>
     * <li>{@link #ONGOING} - 진행중 ({@code startDatetime < now < endDatetime})</li>
     * <li>{@link #UPCOMING} - 예정 ({@code startDatetime > now})</li>
     * <li>{@link #ENDED} - 종료 ({@code endDatetime < now})</li>
     * </ul>
     */
    public enum Status {
        ONGOING, // 진행중
        UPCOMING, // 예정
        ENDED // 종료
    }

    private final AuctionRepository auctionRepository;

    /**
     * 상태 또는 카드 ID 기준으로 경매를 조회한다.
     *
     * <p>
     * 조회 우선순위:
     * </p>
     * <ol>
     * <li>카드 ID(cardId)가 지정된 경우 → 해당 카드와 연결된 경매만 반환</li>
     * <li>그 외에는 상태(status)에 따라 진행중/예정/종료 경매 반환</li>
     * <li>상태 미지정 시 전체 경매 목록 반환</li>
     * </ol>
     *
     * @param status   조회할 경매 상태 (null이면 전체 조회)
     * @param cardId   특정 카드 기준으로 조회할 경우 카드 ID
     * @param pageable 페이징 정보
     * @return 조건에 맞는 경매 페이지 결과
     */
    public Page<Auction> search(Status status, Long cardId, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();

        if (cardId != null) {
            return auctionRepository.findByPhysicalCard_Card_Id(cardId, pageable);
        }

        // 상태에 따른 경매 조회
        if (status == Status.ONGOING) { // 진행중
            return auctionRepository.findByStartDatetimeBeforeAndEndDatetimeAfter(now, now, pageable);
        } else if (status == Status.UPCOMING) { // 예정
            return auctionRepository.findByStartDatetimeAfter(now, pageable);
        } else if (status == Status.ENDED) { // 종료
            return auctionRepository.findByEndDatetimeBefore(now, pageable);
        }

        // 상태 미지정 시 전체 조회
        return auctionRepository.findAll(pageable);
    }
}
