package com.bukadong.tcg.api.auction.controller;

import com.bukadong.tcg.api.auction.dto.request.BidCreateRequest;
import com.bukadong.tcg.api.auction.dto.response.BidResultResponse;
import com.bukadong.tcg.api.auction.service.BidCommandService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auctions")
@RestController
@RequestMapping("/v1/auctions")
@RequiredArgsConstructor
@Validated
public class BidCommandController {
    private final BidCommandService bidCommandService;

    /**
     * 입찰 생성
     * <P>
     * 경매에 신규 입찰을 생성한다. 동시성은 경매 행(PESSIMISTIC_WRITE) 락으로 직렬화한다.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @RETURN BaseResponse<BidResultResponse>
     */
    @Operation(summary = "입찰 생성", description = "경매에 대해 신규 입찰을 생성합니다. 동시성은 DB 행락으로 보장합니다.")
    @PostMapping("/{auctionId}/bids")
    public BaseResponse<BidResultResponse> placeBid(
            @Parameter(description = "경매 ID", required = true, example = "1001") @PathVariable("auctionId") Long auctionId,
            @Parameter(description = "인증 사용자 정보", required = true) @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody BidCreateRequest request) {
        BidResultResponse res = bidCommandService.place(auctionId, user.getUuid(), request);
        return BaseResponse.onSuccess(res);
    }

}
