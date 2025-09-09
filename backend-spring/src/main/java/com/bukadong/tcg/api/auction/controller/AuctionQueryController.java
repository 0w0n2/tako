package com.bukadong.tcg.api.auction.controller;

import com.bukadong.tcg.api.auction.dto.AuctionDto;
import com.bukadong.tcg.api.auction.entity.AuctionStatus;
import com.bukadong.tcg.api.auction.service.AuctionQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.dto.PageResponse;
import com.bukadong.tcg.api.media.dto.MediaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 경매 조회 API (공개)
 *
 * <p>
 * 컨트롤러는 I/O와 응답 포맷만 담당한다.
 * 모든 비즈니스 로직은 서비스(AuctionQueryService)로 위임.
 * </p>
 */
@RestController
@RequestMapping("/v1/auctions")
@RequiredArgsConstructor
public class AuctionQueryController {

    private final AuctionQueryService auctionQueryService;

    /** 경매 목록 조회 (상태/카드 필터 + 페이징) */
    @GetMapping
    public BaseResponse<PageResponse<AuctionDto>> list(
            @RequestParam(required = false) AuctionStatus status,
            @RequestParam(required = false) Long cardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return new BaseResponse<>(auctionQueryService.search(status, cardId, page, size));
    }

    /** 경매 상세 조회 */
    @GetMapping("/{id}")
    public BaseResponse<AuctionDto> get(@PathVariable Long id) {
        return new BaseResponse<>(auctionQueryService.getDetail(id));
    }

    /** 경매 미디어 목록 조회 */
    @GetMapping("/{id}/media")
    public BaseResponse<List<MediaDto>> media(@PathVariable Long id) {
        return new BaseResponse<>(auctionQueryService.getMediaList(id));
    }
}
