package com.bukadong.tcg.api.popularity.controller;

import com.bukadong.tcg.api.popularity.dto.response.PopularCardDto;
import com.bukadong.tcg.api.popularity.service.PopularityService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.dto.PageResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 인기 카드 컨트롤러
 * <P>
 * 이벤트 수집(조회/입찰)과 최근 1시간 인기 카드 조회 API를 제공한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN BaseResponse
 */
@RestController
@RequestMapping("/v1/popularity")
@RequiredArgsConstructor
@Validated
public class PopularityController {

    private final PopularityService popularityService;

    /**
     * 조회 이벤트 기록
     * <P>
     * 해당 경매(연결 카드)의 조회를 현재 분 버킷에 반영한다.
     * </P>
     * 
     * @PARAM categoryId 카테고리 ID
     * @PARAM auctionId 경매 ID
     * @RETURN BaseResponse<Void>
     */
    @PostMapping("/categories/{categoryId}/auctions/{auctionId}/events/view")
    public BaseResponse<Void> recordView(@PathVariable long categoryId,
            @PathVariable long auctionId) {
        popularityService.recordView(categoryId, auctionId);
        return BaseResponse.onSuccess(null);
    }

    /**
     * 입찰 이벤트 기록
     * <P>
     * 해당 경매(연결 카드)의 입찰을 현재 분 버킷에 반영한다.
     * </P>
     * 
     * @PARAM categoryId 카테고리 ID
     * @PARAM auctionId 경매 ID
     * @RETURN BaseResponse<Void>
     */
    @PostMapping("/categories/{categoryId}/auctions/{auctionId}/events/bid")
    public BaseResponse<Void> recordBid(@PathVariable long categoryId,
            @PathVariable long auctionId) {
        popularityService.recordBid(categoryId, auctionId);
        return BaseResponse.onSuccess();
    }

    /**
     * 최근 1시간 인기 카드 목록
     * <P>
     * 카드 ID, rarity(추후), score, imageUrl(추후)만 반환한다.
     * </P>
     * 
     * @PARAM categoryId 카테고리 ID
     * @PARAM page 페이지(0-base)
     * @PARAM size 페이지 크기
     * @RETURN BaseResponse<PageResponse<PopularCardDto>>
     */
    @GetMapping("/categories/{categoryId}/cards")
    public BaseResponse<PageResponse<PopularCardDto>> getTopCardsLastHour(
            @PathVariable long categoryId, @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        PageResponse<PopularCardDto> result = popularityService.getTopCardsLastHour(categoryId,
                page, size);
        return BaseResponse.onSuccess(result);
    }
}
