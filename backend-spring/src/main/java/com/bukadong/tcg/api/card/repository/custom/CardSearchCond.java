package com.bukadong.tcg.api.card.repository.custom;

import lombok.Builder;
import lombok.Getter;

/**
 * 카드 검색 조건
 * <P>
 * 서비스 → 리포지토리 간 전달 전용.
 * </P>
 * 
 * @PARAM categoryMajorId 대분류
 * @PARAM categoryMediumId 중분류
 * @PARAM nameKeyword 이름 접두어
 * @PARAM descriptionKeyword 설명 검색어(FULLTEXT)
 * @RETURN 없음
 */
@Getter
@Builder
public class CardSearchCond {
    private final Long categoryMajorId;
    private final Long categoryMediumId;
    private final String nameKeyword;
    private final String descriptionKeyword;
}
