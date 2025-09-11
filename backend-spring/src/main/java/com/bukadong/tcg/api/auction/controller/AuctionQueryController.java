package com.bukadong.tcg.api.auction.controller;

import com.bukadong.tcg.api.auction.dto.response.AuctionDetailResponse;
import com.bukadong.tcg.api.auction.dto.response.AuctionListItemDto;
import com.bukadong.tcg.api.auction.dto.response.AuctionReviewResponse;
import com.bukadong.tcg.api.auction.repository.AuctionSort;
import com.bukadong.tcg.api.auction.service.AuctionQueryService;
import com.bukadong.tcg.api.auction.service.ReviewQueryService;
import com.bukadong.tcg.api.popularity.aop.AutoPopularityBid;
import com.bukadong.tcg.api.popularity.aop.AutoPopularityView;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 경매 조회 및 입찰 API
 * <P>
 * 컨트롤러는 얇게 유지하고, 비즈니스 로직은 서비스에서 수행한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN BaseResponse로 래핑된 DTO
 */
@Tag(name = "Auctions", description = "경매 조회 및 입찰 API")
@RestController
@RequestMapping("/v1/auctions")
@RequiredArgsConstructor
@Validated
public class AuctionQueryController {

    private final AuctionQueryService auctionQueryService;

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
    @Operation(summary = "경매 목록 조회", description = "카테고리/제목/카드/가격/등급 조건으로 페이지네이션된 경매 목록을 반환합니다. 페이지 당 20개로 고정되며, page는 0-base 입니다.")
    @GetMapping
    public BaseResponse<PageResponse<AuctionListItemDto>> list(
            @Parameter(description = "페이지(0-base)") @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @Parameter(description = "대분류 ID") @RequestParam(name = "categoryMajorId", required = false) Long categoryMajorId,
            @Parameter(description = "중분류 ID") @RequestParam(name = "categoryMediumId", required = false) Long categoryMediumId,
            @Parameter(description = "타이틀 부분검색(키워드)") @RequestParam(name = "title", required = false) String title,
            @Parameter(description = "카드 ID") @RequestParam(name = "cardId", required = false) Long cardId,
            @Parameter(description = "현재가 최소(원)") @RequestParam(name = "currentPriceMin", required = false) BigDecimal currentPriceMin,
            @Parameter(description = "현재가 최대(원)") @RequestParam(name = "currentPriceMax", required = false) BigDecimal currentPriceMax,
            @Parameter(description = "등급 CSV(쉼표 구분). 예: \"PS,NM\"") @RequestParam(name = "grades", required = false) String grades,
            @Parameter(description = "정렬 기준: ENDTIME_ASC | ENDTIME_DESC | BIDCOUNT_DESC | BIDCOUNT_ASC") @RequestParam(name = "sort", required = false) AuctionSort sort) {
        Set<String> gradeSet = (grades == null || grades.isBlank()) ? null
                : Arrays.stream(grades.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());

        var pageData = auctionQueryService.browse(
                categoryMajorId, categoryMediumId, title, cardId,
                currentPriceMin, currentPriceMax, gradeSet, sort, page);

        return BaseResponse.onSuccess(pageData);
    }

    /**
     * 경매 상세 조회
     * <P>
     * 경매/카드/이미지/일주일 시세/입찰 히스토리를 포함한 상세 정보를 조회한다.
     * </P>
     * 
     * @PARAM id 경매 ID (Path)
     * @PARAM historySize 히스토리 개수 (Query, 기본 5)
     * @RETURN BaseResponse<AuctionDetailResponse>
     */
    @AutoPopularityView
    @Operation(summary = "경매 상세 조회", description = "경매/카드/이미지/일주일 시세/입찰 히스토리를 반환합니다.")
    @GetMapping("/{id}")
    public BaseResponse<AuctionDetailResponse> getDetail(
            @Parameter(description = "경매 ID") @PathVariable("id") Long id,
            @Parameter(description = "히스토리 개수(기본 5)") @RequestParam(name = "historySize", required = false, defaultValue = "5") @Min(1) int historySize) {
        return BaseResponse.onSuccess(auctionQueryService.getDetail(id, historySize));
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
    @Operation(summary = "입찰 생성(샘플)", description = "실제 입찰 처리 없이 성공 응답만 반환합니다. (데모/연동용)")
    @PostMapping("/{auctionId}/bids")
    public BaseResponse<Void> placeBid(
            @Parameter(description = "경매 ID", example = "1001") @PathVariable("auctionId") Long auctionId) {
        return BaseResponse.onSuccess();
    }

}
