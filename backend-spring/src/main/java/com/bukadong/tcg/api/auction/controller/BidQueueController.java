package com.bukadong.tcg.api.auction.controller;

import com.bukadong.tcg.api.auction.dto.request.BidQueueRequest;
import com.bukadong.tcg.api.auction.dto.response.BidResultResponse;
import com.bukadong.tcg.api.auction.service.AuctionCacheService;
import com.bukadong.tcg.api.auction.service.BidQueueProducer;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 큐 기반 입찰 컨트롤러
 * <P>
 * Redis Lua 원자 처리 + 비동기 DB 반영 경로.
 * </P>
 */
@Tag(name = "Bids (Queue)", description = "큐 기반(비동기) 입찰 API")
@RestController
@RequestMapping("/v1/auctions")
@RequiredArgsConstructor
public class BidQueueController {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final MemberQueryService memberQueryService;
    private final BidQueueProducer bidQueueProducer;
    private final AuctionCacheService auctionCacheService;

    /**
     * 입찰 생성(큐)
     * <P>
     * 즉시 QUEUED로 응답. 최종 커밋은 컨슈머가 수행.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @RETURN BaseResponse<BidResultResponse>
     */
    @Operation(summary = "입찰 생성(큐)", description = "Redis Lua로 원자 검증/적재 후 비동기로 DB 반영합니다.")
    @PostMapping("/{auctionId}/bids/queue")
    public BaseResponse<BidResultResponse> placeQueued(
            @Parameter(description = "경매 ID", required = true) @PathVariable("auctionId") Long auctionId,
            @AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody BidQueueRequest request) {
        auctionCacheService.ensureLoaded(auctionId);

        Long memberId = memberQueryService.getByUuid(user.getUuid()).getId();
        var r = bidQueueProducer.enqueue(auctionId, memberId, request.getBidPrice(), request.getRequestId());
        String code = r.get("code");

        if ("DUPLICATE".equals(code)) {
            return BaseResponse.onFailure(BaseResponseStatus.AUCTION_DUPLICATE_REQUEST);
        } else if ("NOT_RUNNING".equals(code) || "MISSING".equals(code)) {
            return BaseResponse.onFailure(BaseResponseStatus.AUCTION_NOT_RUNNING);
        } else if ("LOW_PRICE".equals(code)) {
            return BaseResponse.onFailure(BaseResponseStatus.AUCTION_BID_NOT_POSSIBLE_PRICE);
        } else if (!"OK".equals(code)) {
            return BaseResponse.onFailure(BaseResponseStatus.INTERNAL_SERVER_ERROR);
        }

        BigDecimal curAfter = new BigDecimal(r.get("currentPriceAfter"));
        BidResultResponse body = new BidResultResponse(null, // bidId는 비동기 생성
                auctionId, curAfter, LocalDateTime.now(KST), "QUEUED");
        return BaseResponse.onSuccess(body);
    }
}
