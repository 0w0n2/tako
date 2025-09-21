package com.bukadong.tcg.api.admin.card.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 관리자가 새로운 NFT 카드를 발행하고 클레임용 시크릿을 등록하기 위한 요청 DTO
 */
@Schema(description = "관리자 NFT 카드 생성 요청 DTO")
public record NftCreateRequestDto(
        @Schema(description = "card 테이블의 cardId", example = "101")
        @NotNull
        Long cardId
) {
}
