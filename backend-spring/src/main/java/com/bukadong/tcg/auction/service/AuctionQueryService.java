package com.bukadong.tcg.auction.service;

import com.bukadong.tcg.auction.dto.AuctionDto;
import com.bukadong.tcg.auction.entity.Auction;
import com.bukadong.tcg.auction.entity.AuctionStatus;
import com.bukadong.tcg.auction.repository.AuctionRepository;
import com.bukadong.tcg.common.base.BaseResponseStatus;
import com.bukadong.tcg.common.dto.PageResponse;
import com.bukadong.tcg.common.exception.BaseException;
import com.bukadong.tcg.media.dto.MediaDto;
import com.bukadong.tcg.media.entity.MediaType;
import com.bukadong.tcg.media.repository.MediaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    private final AuctionRepository auctionRepository;
    private final MediaRepository mediaRepository;

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
    public Page<Auction> search(AuctionStatus status, Long cardId, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();

        if (cardId != null) {
            return auctionRepository.findByPhysicalCard_Card_Id(cardId, pageable);
        }

        // 상태에 따른 경매 조회
        if (status == AuctionStatus.ONGOING) { // 진행중
            return auctionRepository.findByStartDatetimeBeforeAndEndDatetimeAfter(now, now, pageable);
        } else if (status == AuctionStatus.UPCOMING) { // 예정
            return auctionRepository.findByStartDatetimeAfter(now, pageable);
        } else if (status == AuctionStatus.ENDED) { // 종료
            return auctionRepository.findByEndDatetimeBefore(now, pageable);
        }

        // 상태 미지정 시 전체 조회
        return auctionRepository.findAll(pageable);
    }

    /**
     * 경매 목록 조회 결과를 DTO 페이지로 반환.
     * <p>
     * 여기서 페이징 사이즈 상한 등 정책을 통일 적용한다.
     * </p>
     */
    public PageResponse<AuctionDto> search(AuctionStatus status, Long cardId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100); // [중요 로직] 과도한 size 방지
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<Auction> p = this.search(status, cardId, pageable); // 기존 메서드 재사용
        return PageResponse.from(p.map(AuctionDto::of));
    }

    /**
     * 경매 상세 조회 (DTO 반환).
     *
     * @throws BaseException NOT_FOUND - 존재하지 않음
     */
    public AuctionDto getDetail(Long id) {
        Auction a = auctionRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
        return AuctionDto.of(a);
    }

    /**
     * 경매 미디어 목록 조회 (seqNo 순).
     */
    public List<MediaDto> getMediaList(Long auctionId) {
        return mediaRepository
                .findByTypeAndOwnerIdOrderBySeqNoAsc(MediaType.AUCTION_ITEM, auctionId)
                .stream()
                .map(MediaDto::of)
                .toList();
    }
}
