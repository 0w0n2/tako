package com.bukadong.tcg.api.delivery.controller;

import com.bukadong.tcg.api.delivery.entity.Delivery;
import com.bukadong.tcg.api.delivery.service.DeliveryService;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Delivery")
@RequestMapping("/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final MemberQueryService memberQueryService;

    @Operation(summary = "경매 배송 정보 조회")
    @GetMapping("/{auctionId}")
    public BaseResponse<Delivery> getByAuction(@AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "경매 ID", required = true) @PathVariable("auctionId") long auctionId) {
        var me = memberQueryService.getByUuid(user.getUuid());
        return BaseResponse.onSuccess(deliveryService.getByAuction(me, auctionId));
    }

    @Operation(summary = "판매자: 보내는 주소 설정")
    @PostMapping("/{auctionId}/sender/{addressId}")
    public BaseResponse<Delivery> setSender(@AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "경매 ID", required = true) @PathVariable("auctionId") long auctionId,
            @Parameter(description = "주소 ID", required = true) @PathVariable("addressId") long addressId) {
        var me = memberQueryService.getByUuid(user.getUuid());
        return BaseResponse.onSuccess(deliveryService.setSenderAddress(me, auctionId, addressId));
    }

    @Operation(summary = "구매자: 받는 주소 설정")
    @PostMapping("/{auctionId}/recipient/{addressId}")
    public BaseResponse<Delivery> setRecipient(@AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "경매 ID", required = true) @PathVariable("auctionId") long auctionId,
            @Parameter(description = "주소 ID", required = true) @PathVariable("addressId") long addressId) {
        var me = memberQueryService.getByUuid(user.getUuid());
        return BaseResponse.onSuccess(deliveryService.setRecipientAddress(me, auctionId, addressId));
    }

    @Operation(summary = "판매자: 운송장 등록")
    @PostMapping("/{auctionId}/tracking")
    public BaseResponse<Delivery> setTracking(@AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "경매 ID", required = true) @PathVariable("auctionId") long auctionId,
            @Parameter(description = "운송장 번호", required = true) @RequestParam @NotBlank String trackingNumber) {
        var me = memberQueryService.getByUuid(user.getUuid());
        return BaseResponse.onSuccess(deliveryService.setTrackingNumber(me, auctionId, trackingNumber));
    }

    @Operation(summary = "구매자: 구매 확정")
    @PostMapping("/{auctionId}/confirm")
    public BaseResponse<Void> confirm(@AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "경매 ID", required = true) @PathVariable("auctionId") long auctionId) {
        var me = memberQueryService.getByUuid(user.getUuid());
        deliveryService.confirmByBuyer(me, auctionId);
        return BaseResponse.onSuccess();
    }
}
