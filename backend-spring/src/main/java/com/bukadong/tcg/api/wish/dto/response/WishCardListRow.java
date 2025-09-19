package com.bukadong.tcg.api.wish.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관심 카드 목록 행
 * <P>
 * 목록 응답에 사용. 카드 기본 메타 + 대표 이미지 URL(있으면).
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "WishCardListRow", description = "관심 카드 목록 행")
public class WishCardListRow {

    @Schema(description = "카드 ID", example = "123")
    private Long cardId;

    @Schema(description = "카드 이름", example = "Blue Dragon")
    private String name;

    @Schema(description = "대표 이미지 URL(없으면 null)", example = "https://cdn....")
    private String cardImage;
}
