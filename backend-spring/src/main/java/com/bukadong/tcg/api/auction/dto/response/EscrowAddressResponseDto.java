package com.bukadong.tcg.api.auction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record EscrowAddressResponseDto(
        @Schema(description = "종료된 경매의 에스크로 컨트랙트 주소", example = "0xb51801BEcb46D91231a1231e2092017128D1453e")
        String escrowAddress
) {
    public static EscrowAddressResponseDto toDto(String escrowAddress) {
        return EscrowAddressResponseDto.builder()
                .escrowAddress(escrowAddress)
                .build();
    }
}
