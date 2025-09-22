package com.bukadong.tcg.global.properties.blockchain;

import jakarta.validation.constraints.NotBlank;

/**
 * Sepolia 네트워크 설정
 */
public record SepoliaProperties(
        @NotBlank String rpcUrl,
        @NotBlank String privateKey,
        @NotBlank String walletAddress
) {
}
