package com.bukadong.tcg.api.bid.controller;

import com.bukadong.tcg.api.bid.dto.request.BidQueueRequest;
import com.bukadong.tcg.api.bid.dto.request.BuyNowQueueRequest;
import com.bukadong.tcg.api.bid.dto.response.BidResultResponse;
import com.bukadong.tcg.api.bid.entity.AuctionBidReason;
import com.bukadong.tcg.api.bid.service.AuctionCacheService;
import com.bukadong.tcg.api.bid.service.BidQueueProducer;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.bukadong.tcg.api.popularity.aop.AutoPopularityBid;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
@Slf4j
public class BidQueueController {

    private final MemberQueryService memberQueryService;
    private final BidQueueProducer bidQueueProducer;
    private final AuctionCacheService auctionCacheService;
    private final com.bukadong.tcg.api.auction.repository.AuctionRepository auctionRepository;
    private static final ZoneOffset UTC = ZoneOffset.UTC;

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

        var member = memberQueryService.getByUuid(user.getUuid());
        if (member.getWalletAddress() == null || member.getWalletAddress().isBlank()) {
            return BaseResponse.onFailure(BaseResponseStatus.WALLET_ADDRESS_NOT_FOUND);
        }
        Long memberId = member.getId();
        var r = bidQueueProducer.enqueue(auctionId, memberId, request.getAmount(), request.getRequestId());
        String code = r.get("code");

        // 1회 리로드 후 재시도: NOT_RUNNING 또는 MISSING 이면서 DB 기준 진행중일 수 있는 경우 캐시 싱크 후 재시도
        if (AuctionBidReason.NOT_RUNNING.name().equals(code) || AuctionBidReason.MISSING.name().equals(code)) {
            var db = auctionRepository.findById(auctionId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
            boolean dbRunning = db.isRunningAt(LocalDateTime.now(UTC));
            if (dbRunning) {
                auctionCacheService.reloadFromDb(auctionId);
                var retry = bidQueueProducer.enqueue(auctionId, memberId, request.getAmount(), request.getRequestId());
                String rcode = retry.get("code");
                if (!AuctionBidReason.NOT_RUNNING.name().equals(rcode)
                        && !AuctionBidReason.MISSING.name().equals(rcode)) {
                    r = retry; // 재시도 성공 경로 계속 진행
                    code = rcode;
                } else {
                    // 여전히 실패면 상태 로깅
                    var snap = auctionCacheService.getSnapshot(auctionId);
                    log.warn(
                            "Bid NOT_RUNNING after reload: auctionId={}, redisSnap={}, db.isEnd={}, db.start={}, db.end={}, nowUTC={}",
                            auctionId, snap, db.isEnd(), db.getStartDatetime(), db.getEndDatetime(),
                            LocalDateTime.now(UTC));
                }
            }
        }

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
                auctionId, curAfter, LocalDateTime.now(UTC), "QUEUED");
        return BaseResponse.onSuccess(body);
    }

    /**
     * 즉시구매(큐)
     * <p>
     * buy_now_flag=1인 경매에서만 허용. 요청자는 본인 경매에 구매할 수 없음.
     * </p>
     */
    @Operation(summary = "즉시구매(큐)", description = "buy_now_flag=1인 경매에서 즉시구매 가격으로 입찰을 큐에 적재합니다.")
    @PostMapping("/{auctionId}/buy-now/queue")
    public BaseResponse<BidResultResponse> buyNowQueued(
            @Parameter(description = "경매 ID", required = true) @PathVariable("auctionId") Long auctionId,
            @AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody BuyNowQueueRequest request) {
        auctionCacheService.ensureLoaded(auctionId);

        var me = memberQueryService.getByUuid(user.getUuid());
        if (me.getWalletAddress() == null || me.getWalletAddress().isBlank()) {
            return BaseResponse.onFailure(BaseResponseStatus.WALLET_ADDRESS_NOT_FOUND);
        }
        var auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        if (!auction.isBuyNowFlag() || auction.getBuyNowPrice() == null) {
            return BaseResponse.onFailure(BaseResponseStatus.AUCTION_NOT_RUNNING);
        }
        if (auction.getMember() != null && auction.getMember().getId().equals(me.getId())) {
            return BaseResponse.onFailure(BaseResponseStatus.AUCTION_BID_FORBIDDEN);
        }
        if (auction.isEnd()) {
            return BaseResponse.onFailure(BaseResponseStatus.AUCTION_NOT_RUNNING);
        }

        var r = bidQueueProducer.enqueue(auctionId, me.getId(), auction.getBuyNowPrice(), request.getRequestId());
        String code = r.get("code");

        // 즉시구매도 동일한 재시도 전략 적용
        if (AuctionBidReason.NOT_RUNNING.name().equals(code) || AuctionBidReason.MISSING.name().equals(code)) {
            boolean dbRunning = !auction.isEnd() && auction.isRunningAt(LocalDateTime.now(UTC));
            if (dbRunning) {
                auctionCacheService.reloadFromDb(auctionId);
                var retry = bidQueueProducer.enqueue(auctionId, me.getId(), auction.getBuyNowPrice(),
                        request.getRequestId());
                String rcode = retry.get("code");
                if (!AuctionBidReason.NOT_RUNNING.name().equals(rcode)
                        && !AuctionBidReason.MISSING.name().equals(rcode)) {
                    r = retry;
                    code = rcode;
                } else {
                    var snap = auctionCacheService.getSnapshot(auctionId);
                    log.warn(
                            "BuyNow NOT_RUNNING after reload: auctionId={}, redisSnap={}, db.isEnd={}, db.start={}, db.end={}, nowUTC={}",
                            auctionId, snap, auction.isEnd(), auction.getStartDatetime(), auction.getEndDatetime(),
                            LocalDateTime.now(UTC));
                }
            }
        }

        if (AuctionBidReason.DUPLICATE.name().equals(code)) {
            return BaseResponse.onFailure(BaseResponseStatus.AUCTION_DUPLICATE_REQUEST);
        } else if (AuctionBidReason.NOT_RUNNING.name().equals(code) || AuctionBidReason.MISSING.name().equals(code)) {
            return BaseResponse.onFailure(BaseResponseStatus.AUCTION_NOT_RUNNING);
        } else if (AuctionBidReason.LOW_PRICE.name().equals(code)) {
            // buy-now 경로에서 LOW_PRICE는 로직상 나오지 않으나 안전하게 처리
            return BaseResponse.onFailure(BaseResponseStatus.AUCTION_BID_NOT_POSSIBLE_PRICE);
        } else if (AuctionBidReason.SELF_BID.name().equals(code)) {
            return BaseResponse.onFailure(BaseResponseStatus.AUCTION_BID_FORBIDDEN);
        } else if (!AuctionBidReason.OK.name().equals(code)) {
            return BaseResponse.onFailure(BaseResponseStatus.INTERNAL_SERVER_ERROR);
        }

        BigDecimal curAfter = new BigDecimal(r.get("currentPriceAfter"));
        BidResultResponse body = new BidResultResponse(null, auctionId, curAfter, LocalDateTime.now(UTC), "QUEUED");
        return BaseResponse.onSuccess(body);
    }
}
