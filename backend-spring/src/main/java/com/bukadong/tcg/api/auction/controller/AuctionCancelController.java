package com.bukadong.tcg.api.auction.controller;

import com.bukadong.tcg.api.auction.dto.response.AuctionCancelResponse;
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
 * 경매 취소(사용자)
 * <P>
 * 본인 경매 & 미종료 & 무입찰 & 종료 전 조건에서만 취소 가능.
 * </P>
 */
@Tag(name = "Auctions", description = "경매 조회 및 입찰/취소 API")
@RestController
@RequestMapping("/v1/auctions")
@RequiredArgsConstructor
@Validated
public class AuctionCancelController {

    private final AuctionCancelService auctionCancelService;
    private final MemberQueryService memberQueryService;

    /**
     * 사용자 경매 취소
     * <P>
     * 조건 만족 시 경매를 취소(종료) 처리한다.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @RETURN BaseResponse<AuctionCancelResponse>
     */
    @Operation(summary = "경매 취소(사용자)", description = "본인 경매이며 미종료·무입찰·종료시간 이전인 경우에만 취소됩니다.")
    @PostMapping("/{auctionId}/cancel")
    public BaseResponse<AuctionCancelResponse> cancelMyAuction(
            @Parameter(description = "경매 ID", required = true) @PathVariable("auctionId") @Min(1) Long auctionId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = memberQueryService.getByUuid(user.getUuid()).getId();
        AuctionCancelResponse res = auctionCancelService.cancelByOwner(auctionId, memberId);
        return BaseResponse.onSuccess(res);
    }
}
