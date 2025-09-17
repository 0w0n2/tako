package com.bukadong.tcg.api.auction.controller;

import com.bukadong.tcg.api.auction.dto.response.AuctionCancelCheckResponse;
import com.bukadong.tcg.api.auction.service.AuctionCancelService;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 경매 취소 가능 여부 조회(사용자)
 * <P>
 * 본인/종료 여부/시간/입찰 존재를 점검하고 결과를 반환.
 * </P>
 */
@Tag(name = "Auctions")
@RestController
@RequestMapping("/v1/auctions")
@RequiredArgsConstructor
@Validated
public class AuctionCancelQueryController {

    private final AuctionCancelService auctionCancelService;
    private final MemberQueryService memberQueryService;

    /**
     * 취소 가능 여부 조회
     * <P>
     * 항상 200 OK로 결과를 반환(없는 경매는 404).
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @RETURN BaseResponse<AuctionCancelCheckResponse>
     */
    @Operation(summary = "경매 취소 가능 여부(사용자)", description = "본인/시간/입찰 여부를 검사해 취소 가능 상태를 반환합니다.")
    @GetMapping("/{auctionId}/cancel/check")
    public BaseResponse<AuctionCancelCheckResponse> checkCancelable(
            @Parameter(description = "경매 ID", required = true) @PathVariable("auctionId") @Min(1) Long auctionId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = memberQueryService.getByUuid(user.getUuid()).getId();
        AuctionCancelCheckResponse res = auctionCancelService.checkCancelableByOwner(auctionId, memberId);
        return BaseResponse.onSuccess(res);
    }
}
