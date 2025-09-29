package com.bukadong.tcg.api.admin.card.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 발행한 NFT를 클레임할 수 있는 시크릿 코드의 해시를 등록
 *
 * @param tokenId 시크릿을 등록할 NFT의 ID
 * @param secret  해시되지 않은 원본 시크릿 코드(keccak256)
 */
public record RegisterSecretRequestDto(
        @NotNull Long tokenId,
        @NotBlank String secret
) {
}
