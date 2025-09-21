package com.bukadong.tcg.api.admin.card.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 관리자 NFT 카드 생성 API의 성공 응답 DTO
 * 생성된 NFT tokenId, 트랜잭션 해시, 실물 카드에 인쇄할 시크릿 코드 반환
 */
@Builder
@Schema(description = "관리자 NFT 생성 요청 접수 응답 DTO")
public record NftCreateResponseDto(
        @Schema(description = "DB에 생성된 실물 카드 ID")
        Long physicalCardId,

        @Schema(description = "생성된 NFT 토큰 ID")
        Long tokenId,

        @Schema(description = "실물 카드에 인쇄하거나 사용자에게 전달해야 할 원본 시크릿 코드")
        String secret
) {
}
