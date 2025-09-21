package com.bukadong.tcg.api.admin.blockchain.controller;

import com.bukadong.tcg.api.admin.blockchain.dto.response.LatestBlockResponseDto;
import com.bukadong.tcg.api.admin.blockchain.service.BlockChainService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin")
@RestController
@RequestMapping("/v1/admin/blockchain")
@RequiredArgsConstructor
public class AdminBlockChainController {

    private final BlockChainService blockChainService;

    @GetMapping("/latest-block")
    public BaseResponse<LatestBlockResponseDto> getLatestBlock() {
        return BaseResponse.onSuccess(LatestBlockResponseDto.toDto(blockChainService.getLatestBlockNumber()));
    }
}
