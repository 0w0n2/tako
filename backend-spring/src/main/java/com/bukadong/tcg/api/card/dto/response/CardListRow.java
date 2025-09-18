package com.bukadong.tcg.api.card.dto.response;

import com.bukadong.tcg.api.card.entity.CardAttribute;
import com.bukadong.tcg.api.card.entity.Rarity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 카드 목록 행 DTO
 * <P>
 * 검색 결과 행 + FULLTEXT 점수(score) 포함.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Getter
@AllArgsConstructor
public class CardListRow {

    @Schema(description = "카드 ID")
    private Long id;

    @Schema(description = "카드명")
    private String name;

    @Schema(description = "카드 코드")
    private String code;

    @Schema(description = "속성")
    private CardAttribute attribute;

    @Schema(description = "희귀도")
    private Rarity rarity;

    @Schema(description = "전문검색 점수(설명 검색 시)", example = "5.321")
    private Double score;

    @Schema(description = "관심 등록 여부(멤버별, 인증된 경우에만)", example = "true")
    private boolean wished;
}
