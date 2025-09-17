package com.bukadong.tcg.api.bid.controller;

import com.bukadong.tcg.api.bid.dto.request.BidQueueRequest;
import com.bukadong.tcg.api.bid.dto.response.BidResultResponse;
import com.bukadong.tcg.api.bid.entity.AuctionBidReason;
import com.bukadong.tcg.api.bid.service.AuctionCacheService;
import com.bukadong.tcg.api.bid.service.BidQueueProducer;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.bukadong.tcg.api.popularity.aop.AutoPopularityBid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 큐 기반 입찰 컨트롤러
 * <P>
 * Redis Lua 원자 처리 + 비동기 DB 반영 경로.
 * </P>
 */
@Tag(name = "Auctions")
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
    @AutoPopularityBid
    @Operation(summary = "입찰 생성(큐)", description = "Redis Lua로 원자 검증/적재 후 비동기로 DB 반영합니다.")
    @PostMapping("/{auctionId}/bids/queue")
    public BaseResponse<BidResultResponse> placeQueued(
            @Parameter(description = "경매 ID", required = true) @PathVariable("auctionId") Long auctionId,
            @AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody BidQueueRequest request) {
        auctionCacheService.ensureLoaded(auctionId);

        Long memberId = memberQueryService.getByUuid(user.getUuid()).getId();
        var r = bidQueueProducer.enqueue(auctionId, memberId, request.getBidPrice(), request.getRequestId());
        String code = r.get("code");

        if (AuctionBidReason.DUPLICATE.name().equals(code)) {
            return BaseResponse.onFailure(BaseResponseStatus.AUCTION_DUPLICATE_REQUEST);
        } else if (AuctionBidReason.NOT_RUNNING.name().equals(code) || AuctionBidReason.MISSING.name().equals(code)) {
            return BaseResponse.onFailure(BaseResponseStatus.AUCTION_NOT_RUNNING);
        } else if (AuctionBidReason.LOW_PRICE.name().equals(code)) {
            return BaseResponse.onFailure(BaseResponseStatus.AUCTION_BID_NOT_POSSIBLE_PRICE);
        } else if (AuctionBidReason.SELF_BID.name().equals(code)) {
            return BaseResponse.onFailure(BaseResponseStatus.AUCTION_BID_FORBIDDEN);
        } else if (!AuctionBidReason.OK.name().equals(code)) {
            return BaseResponse.onFailure(BaseResponseStatus.INTERNAL_SERVER_ERROR);
        }

        BigDecimal curAfter = new BigDecimal(r.get("currentPriceAfter"));
        BidResultResponse body = new BidResultResponse(null, // bidId는 비동기 생성
                auctionId, curAfter, LocalDateTime.now(KST), "QUEUED");
        return BaseResponse.onSuccess(body);
    }
}
