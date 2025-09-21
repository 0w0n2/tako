package com.bukadong.tcg.api.admin.card.dto.response;

import com.bukadong.tcg.api.card.entity.PhysicalCard;
import com.bukadong.tcg.api.card.entity.PhysicalCardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Builder
@Schema(description = "실물 카드 상태 조회 응답 DTO")
public record PhysicalCardStatusResponseDto(
        @Schema(description = "실물 카드 DB ID")
        Long physicalCardId,

        @Schema(description = "블록체인 상의 토큰 ID (발행 전엔 null)")
        BigInteger tokenId,

        @Schema(description = "NFT 발행 상태 (PENDING, MINTED, CLAIMED, FAILED)")
        PhysicalCardStatus status,

        @Schema(description = "최종 업데이트 시각")
        LocalDateTime updatedAt
) {
    public static PhysicalCardStatusResponseDto toDto(PhysicalCard physicalCard) {
        return PhysicalCardStatusResponseDto.builder()
                .physicalCardId(physicalCard.getId())
                .status(physicalCard.getStatus())
                .tokenId(physicalCard.getTokenId())
                .updatedAt(physicalCard.getUpdatedAt())
                .build();
    }
}
