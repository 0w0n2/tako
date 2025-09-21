package com.bukadong.tcg.api.card.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 사용자가 NFT 소유권을 주장(Claim)하기 위한 요청 DTO
 */
public record NftClaimRequestDto(
        @NotNull Long tokenId,
        @NotBlank String secret
) {
}
