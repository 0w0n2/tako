package com.bukadong.tcg.api.admin.card.controller;

import com.bukadong.tcg.api.card.service.PhysicalCardService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/physical-cards")
public class AdminPhysicalCardController {

    private final PhysicalCardService physicalCardService;

    @Operation(summary = "특정 실물 카드(NFT)의 상태 조회", description = "physicalCardId로 NFT의 발행 상태(PENDING, MINTED 등)를 조회합니다.")
    @GetMapping("/{physicalCardId}")
    public BaseResponse<?> getNftStatus(@PathVariable("physicalCardId") Long physicalCardId) {
        return BaseResponse.onSuccess(physicalCardService.getCardStatusForAdmin(physicalCardId));
    }
}
