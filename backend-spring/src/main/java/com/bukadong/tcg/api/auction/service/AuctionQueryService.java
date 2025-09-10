package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.dto.response.AuctionListItemDto;
import com.bukadong.tcg.api.auction.dto.response.AuctionListRow;
import com.bukadong.tcg.api.auction.repository.AuctionRepositoryCustom;
import com.bukadong.tcg.api.auction.repository.AuctionSort;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.dto.PageResponse;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 경매 목록 조회 서비스
 * <P>
 * 파라미터 검증/페이지 강제/남은시간 계산을 담당한다.
 * </P>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionQueryService {

    private final AuctionRepositoryCustom auctionRepositoryCustom;

    /**
     * 경매 목록 조회
     * <P>
     * 페이지 크기는 20으로 고정한다.
     * </P>
     *
     * @param categoryMajorId  대분류 ID
     * @param categoryMediumId 중분류 ID
     * @param titlePart        타이틀 부분검색
     * @param cardId           카드 ID
     * @param currentPriceMin  현재가 최소
     * @param currentPriceMax  현재가 최대
     * @param grades           등급 집합(예: PS,NM)
     * @param sort             정렬 기준
     * @param page             페이지(0-base)
     * @return PageResponse로 감싼 응답 DTO 페이지
     */
    public PageResponse<AuctionListItemDto> browse(Long categoryMajorId, Long categoryMediumId,
            String titlePart, Long cardId, BigDecimal currentPriceMin, BigDecimal currentPriceMax,
            Set<String> grades, AuctionSort sort, int page) {
        if (currentPriceMin != null && currentPriceMax != null
                && currentPriceMin.compareTo(currentPriceMax) > 0) {
            throw new BaseException(BaseResponseStatus.BAD_REQUEST);
        }

        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(safePage, 20);

        var rowsPage = auctionRepositoryCustom.searchAuctions(categoryMajorId, categoryMediumId,
                titlePart, cardId, currentPriceMin, currentPriceMax, grades,
                sort == null ? AuctionSort.ENDTIME_ASC : sort, pageable);

        LocalDateTime now = LocalDateTime.now();
        var dtoPage = rowsPage.map(r -> toListItem(now, r));

        return PageResponse.from(dtoPage);
    }

    /**
     * 내부 행 DTO를 응답 DTO로 변환한다.
     * <P>
     * 남은 시간(초)을 now 기준으로 계산한다.
     * </P>
     *
     * @param now 현재 시각
     * @param r   내부 행 DTO
     * @return 목록 응답 DTO
     */
    private static AuctionListItemDto toListItem(LocalDateTime now, AuctionListRow r) {
        long remain = Math.max(0, Duration.between(now, r.endDatetime()).getSeconds());
        return new AuctionListItemDto(r.id(), r.grade(), r.title(), r.currentPrice(), r.bidCount(),
                remain, r.primaryImageUrl());
    }
}
