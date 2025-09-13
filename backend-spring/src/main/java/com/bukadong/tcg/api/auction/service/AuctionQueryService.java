package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.converter.AuctionListConverter;
import com.bukadong.tcg.api.auction.dto.response.AuctionDetailResponse;
import com.bukadong.tcg.api.auction.dto.response.AuctionListItemResponse;
import com.bukadong.tcg.api.auction.repository.AuctionDetailRepository;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.auction.repository.AuctionRepositoryCustom;
import com.bukadong.tcg.api.auction.repository.AuctionSort;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaUrlService;
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
import java.util.List;
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
    private final AuctionDetailRepository auctionDetailRepository;
    private final AuctionRepository auctionRepository; // 상세 조회용 fetch-graph 사용
    private final MediaUrlService mediaUrlService;

    /**
     * 경매 목록 조회 서비스(컨트롤러 편의 오버로드)
     * <P>
     * 컨트롤러가 page(int)만 넘겨도 되도록 내부에서 Pageable(size=20)을 구성하고, PageResponse로 변환해
     * 반환합니다. 동일 클래스 내 트랜잭션 메서드 호출을 피하여 Sonar 규칙(java:S6809)도 만족합니다.
     * </P>
     * 
     * @PARAM categoryMajorId 카테고리 대분류 ID
     * @PARAM categoryMediumId 카테고리 중분류 ID
     * @PARAM titlePart 타이틀 부분검색어
     * @PARAM cardId 카드 ID
     * @PARAM currentPriceMin 현재가 최소
     * @PARAM currentPriceMax 현재가 최대
     * @PARAM grades 등급 집합
     * @PARAM sort 정렬 기준 (null이면 ENDTIME_ASC)
     * @PARAM page 0-base 페이지 인덱스
     * @RETURN PageResponse<AuctionListItemResponse>
     */
    public PageResponse<AuctionListItemResponse> getAuctionList(Long categoryMajorId, Long categoryMediumId,
            String titlePart, Long cardId, BigDecimal currentPriceMin, BigDecimal currentPriceMax, Set<String> grades,
            AuctionSort sort, int page) {
        Pageable pageable = PageRequest.of(page, 20);

        var rows = auctionRepositoryCustom.searchAuctions(categoryMajorId, categoryMediumId, titlePart, cardId,
                currentPriceMin, currentPriceMax, grades, (sort == null ? AuctionSort.ENDTIME_ASC : sort), pageable);

        Duration ttl = Duration.ofMinutes(30);
        List<AuctionListItemResponse> items = rows.getContent().stream()
                .map(row -> AuctionListConverter.toItem(row, mediaUrlService, ttl)).toList();

        Page<AuctionListItemResponse> p = new PageImpl<>(items, pageable, rows.getTotalElements());

        return PageResponse.from(p);
    }

    /**
     * 경매 상세 조회
     * <P>
     * 히스토리는 기본 5개이며, 요청 파라미터로 개수 조절 가능.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @PARAM historySize 히스토리 개수
     * @RETURN AuctionDetailResponse
     */
    public AuctionDetailResponse getDetail(Long auctionId, int historySize) {
        var auction = auctionRepository.findByIdWithCardAndCategory(auctionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        var auctionInfo = auctionDetailRepository.mapAuctionInfo(auction);
        var cardInfo = auctionDetailRepository.mapCardInfo(auction);

        List<String> imageUrls = mediaUrlService.getPresignedImageUrls(MediaType.AUCTION_ITEM, auctionId,
                Duration.ofMinutes(5));
        var weeklyPrices = auctionDetailRepository.findWeeklyPriceLinesByCardId(auction.getCard().getId());
        var history = auctionDetailRepository.findBidHistory(auctionId, historySize);
        var sellerInfo = auctionDetailRepository.findSellerInfoByAuctionId(auctionId);

        return AuctionDetailResponse.builder().auction(auctionInfo).card(cardInfo).weeklyPrices(weeklyPrices)
                .history(history).imageUrls(imageUrls).seller(sellerInfo).build();
    }
}
