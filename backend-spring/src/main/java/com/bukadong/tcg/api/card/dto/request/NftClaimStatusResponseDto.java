package com.bukadong.tcg.api.card.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "NFT 클레임 가능 상태 조회 응답 DTO")
public record NftClaimStatusResponseDto(
        @Schema(description = "클레임 가능 여부")
        boolean isClaimable,

        @Schema(description = "상태에 대한 메시지", example = "클레임 가능한 NFT입니다.")
        String message
) {
}
