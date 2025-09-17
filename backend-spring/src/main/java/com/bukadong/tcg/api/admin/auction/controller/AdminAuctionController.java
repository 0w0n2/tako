package com.bukadong.tcg.api.admin.auction.controller;

import com.bukadong.tcg.api.auction.dto.response.AuctionCancelResponse;
import com.bukadong.tcg.api.auction.service.AuctionCancelService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 경매 강제 종료(관리자)
 * <P>
 * 관리자 권한으로 즉시 종료 처리. (보안은 Security 설정에 따름)
 * </P>
 */
@Tag(name = "Admin")
@RestController
@RequestMapping("/v1/admin/auctions")
@RequiredArgsConstructor
@Validated
public class AdminAuctionController {

    private final AuctionCancelService auctionCancelService;

    /**
     * 관리자 강제 종료
     * <P>
     * 조건 검증 없이 is_end=1(종료) 처리.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @RETURN BaseResponse<AuctionCancelResponse>
     */
    @Operation(summary = "경매 강제 종료(관리자)", description = "관리자 권한으로 즉시 종료합니다.")
    @PostMapping("/{auctionId}/cancel")
    public BaseResponse<AuctionCancelResponse> adminCancel(
            @Parameter(description = "경매 ID", required = true) @PathVariable("auctionId") @Min(1) Long auctionId) {
        AuctionCancelResponse res = auctionCancelService.cancelByAdmin(auctionId);
        return BaseResponse.onSuccess(res);
    }
}
