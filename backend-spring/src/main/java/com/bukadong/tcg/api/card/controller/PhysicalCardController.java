package com.bukadong.tcg.api.card.controller;

import com.bukadong.tcg.api.card.dto.request.NftClaimStatusResponseDto;
import com.bukadong.tcg.api.card.dto.response.NftAuctionHistoryResponseDto;
import com.bukadong.tcg.api.card.service.PhysicalCardService;
import com.bukadong.tcg.global.blockchain.service.TakoNftContractService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "PhysicalCard & NFT")
@RequestMapping("/v1/physical-cards")
@RequiredArgsConstructor
@RestController
public class PhysicalCardController {
    private final PhysicalCardService physicalCardService;
    private final TakoNftContractService takoNftContractService;


    @Operation(summary = "NFT 클레임 가능 상태 조회", description = "사용자가 특정 tokenId를 클레임할 수 있는지 미리 확인합니다.")
    @GetMapping("/{tokenId}/claim-status")
    public BaseResponse<NftClaimStatusResponseDto> getClaimStatus(@PathVariable("tokenId") Long tokenId) {
        return BaseResponse.onSuccess(physicalCardService.checkClaimStatus(tokenId));
    }

    @Operation(summary = "NFT 경매 이력 조회", description = "특정 tokenId를 가진 NFT의 모든 온체인 경매 이력을 조회합니다.")
    @GetMapping("/{tokenId}/history")
    public BaseResponse<List<NftAuctionHistoryResponseDto>> getAuctionHistory(@PathVariable("tokenId") Long tokenId) {
        return BaseResponse.onSuccess(takoNftContractService.getAuctionHistories(tokenId));
    }
}
