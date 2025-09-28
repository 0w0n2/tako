package com.bukadong.tcg.api.admin.blockchain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigInteger;

@Builder
public record LatestBlockResponseDto(
        @Schema(name = "blockNumber", description = "최신 블록 번호", example = "9232327")
        String blockNumber
) {
    public static LatestBlockResponseDto toDto(BigInteger blockNumber) {
        return LatestBlockResponseDto.builder()
                .blockNumber(String.valueOf(blockNumber))
                .build();
    }
}
