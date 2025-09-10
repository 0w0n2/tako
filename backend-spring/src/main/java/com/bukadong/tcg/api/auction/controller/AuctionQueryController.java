package com.bukadong.tcg.api.auction.controller;

import com.bukadong.tcg.api.auction.dto.response.AuctionListItemDto;
import com.bukadong.tcg.api.auction.repository.AuctionSort;
import com.bukadong.tcg.api.auction.service.AuctionQueryService;
import com.bukadong.tcg.api.popularity.aop.AutoPopularityBid;
import com.bukadong.tcg.api.popularity.aop.AutoPopularityView;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.dto.PageResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 경매 목록 조회 API (QueryDSL)
 * <P>
 * 동적 필터/정렬/페이지네이션을 제공한다.
 * </P>
 */
@RestController
@RequestMapping("/v1/auctions")
@RequiredArgsConstructor
public class AuctionQueryController {

    private final AuctionQueryService service;

    /**
     * 경매 목록 조회
     * <P>
     * 페이지 당 20개로 고정된다.
     * </P>
     *
     * @param page             페이지(0-base)
     * @param categoryMajorId  대분류 ID
     * @param categoryMediumId 중분류 ID
     * @param title            타이틀 부분검색
     * @param cardId           카드 ID
     * @param currentPriceMin  현재가 최소
     * @param currentPriceMax  현재가 최대
     * @param grades           등급 CSV(예: "PS,NM")
     * @param sort             정렬
     *                         (ENDTIME_ASC|ENDTIME_DESC|BIDCOUNT_DESC|BIDCOUNT_ASC)
     * @return BaseResponse로 감싼 PageResponse
     */
    @GetMapping
    public BaseResponse<PageResponse<AuctionListItemDto>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false) Long categoryMajorId,
            @RequestParam(required = false) Long categoryMediumId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long cardId,
            @RequestParam(required = false) BigDecimal currentPriceMin,
            @RequestParam(required = false) BigDecimal currentPriceMax,
            @RequestParam(required = false) String grades,
            @RequestParam(required = false) AuctionSort sort) {
        Set<String> gradeSet = (grades == null || grades.isBlank()) ? null
                : Arrays.stream(grades.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());

        var pageData = service.browse(categoryMajorId, categoryMediumId, title, cardId,
                currentPriceMin, currentPriceMax, gradeSet, sort, page);
        return new BaseResponse<>(pageData);
    }

    /**
     * 경매 상세 조회 (깡통)
     * <P>
     * 실제 상세 로직 없이 성공 응답만 반환한다.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @RETURN BaseResponse<Void>
     */
    @AutoPopularityView
    @GetMapping("/{auctionId}")
    public BaseResponse<Void> getDetail(@PathVariable Long auctionId) {
        return BaseResponse.onSuccess();
    }

    /**
     * 입찰 생성 (깡통)
     * <P>
     * 실제 입찰 처리 없이 성공 응답만 반환한다.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @RETURN BaseResponse<Void>
     */
    @AutoPopularityBid
    @PostMapping("/{auctionId}/bids")
    public BaseResponse<Void> placeBid(@PathVariable Long auctionId) {
        return BaseResponse.onSuccess();
    }

}
