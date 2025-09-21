package com.bukadong.tcg.global.properties.blockchain;

import jakarta.validation.constraints.NotBlank;

/**
 * 경매 관련 컨트랙트 주소
 */
public record AuctionProperties(
        @NotBlank String factory
) {
}
