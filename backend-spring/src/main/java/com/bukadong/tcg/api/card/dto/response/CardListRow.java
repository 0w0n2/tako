package com.bukadong.tcg.api.card.dto.response;

import java.util.Collections;
import java.util.List;

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

    @Schema(description = "이미지 URL 목록(임시 인증 URL, 기본 5분 유효)", example = "[\"https://example.com/presigned1\",\"https://example.com/presigned2\"]")
    private List<String> imageUrls = Collections.emptyList();

    // 기존 리포지토리/QueryDSL 프로젝션과 호환을 위한 7-인자 생성자 유지
    public CardListRow(Long id, String name, String code, CardAttribute attribute, Rarity rarity, Double score,
            boolean wished) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.attribute = attribute;
        this.rarity = rarity;
        this.score = score;
        this.wished = wished;
        this.imageUrls = Collections.emptyList();
    }

    // Service에서 주입하기 위한 setter
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = (imageUrls == null) ? Collections.emptyList() : imageUrls;
    }
}
