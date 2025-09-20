package com.bukadong.tcg.api.auction.controller;

import com.bukadong.tcg.api.auction.dto.response.AuctionDetailResponse;
import com.bukadong.tcg.api.auction.dto.response.AuctionListItemResponse;
import com.bukadong.tcg.api.auction.repository.AuctionSort;
import com.bukadong.tcg.api.auction.service.AuctionQueryService;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.api.popularity.aop.AutoPopularityView;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.dto.PageResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
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
    private final MemberQueryService memberQueryService;

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
    public BaseResponse<PageResponse<AuctionListItemResponse>> getList(
            @Parameter(description = "페이지(0-base)") @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @Parameter(description = "대분류 ID") @RequestParam(name = "categoryMajorId", required = false) Long categoryMajorId,
            @Parameter(description = "중분류 ID") @RequestParam(name = "categoryMediumId", required = false) Long categoryMediumId,
            @Parameter(description = "타이틀 부분검색(키워드)") @RequestParam(name = "title", required = false) String title,
            @Parameter(description = "카드 ID") @RequestParam(name = "cardId", required = false) Long cardId,
            @Parameter(description = "현재가 최소(원)") @RequestParam(name = "currentPriceMin", required = false) BigDecimal currentPriceMin,
            @Parameter(description = "현재가 최대(원)") @RequestParam(name = "currentPriceMax", required = false) BigDecimal currentPriceMax,
            @Parameter(description = "등급 CSV(쉼표 구분). 예: \"PS,NM\"") @RequestParam(name = "grades", required = false) String grades,
            @Parameter(description = "정렬 기준: ENDTIME_ASC | ENDTIME_DESC | BIDCOUNT_DESC | BIDCOUNT_ASC") @RequestParam(name = "sort", required = false) AuctionSort sort,
            @AuthenticationPrincipal CustomUserDetails user) {
        Set<String> gradeSet = (grades == null || grades.isBlank()) ? null
                : Arrays.stream(grades.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());
        Long memberId = (user == null) ? null : memberQueryService.getByUuid(user.getUuid()).getId();

        var pageData = auctionQueryService.getAuctionList(categoryMajorId, categoryMediumId, title, cardId,
                currentPriceMin, currentPriceMax, gradeSet, sort, page, memberId);

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
    @GetMapping("/{auctionId}")
    public BaseResponse<AuctionDetailResponse> getDetail(
            @Parameter(description = "경매 ID") @PathVariable("auctionId") Long auctionId,
            @Parameter(description = "히스토리 개수(기본 5)") @RequestParam(name = "historySize", required = false, defaultValue = "5") @Min(1) int historySize,
            @AuthenticationPrincipal CustomUserDetails user) {

        Long memberId = (user == null) ? null : memberQueryService.getByUuid(user.getUuid()).getId();
        return BaseResponse.onSuccess(auctionQueryService.getDetail(auctionId, historySize, memberId));
    }

}
