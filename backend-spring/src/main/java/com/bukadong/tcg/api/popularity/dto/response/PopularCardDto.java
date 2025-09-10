package com.bukadong.tcg.api.popularity.dto.response;

/**
 * 인기 카드 DTO
 * <P>
 * 최근 1시간 누적 점수 기준의 카드 요약을 제공한다.
 * </P>
 * 
 * @PARAM cardId 카드 ID
 * @PARAM rarity 희귀도(추후 컬럼/조인으로 채울 예정)
 * @PARAM score 최근 1시간 합산 점수
 * @PARAM imageUrl 대표 이미지 URL(추후 미디어 조인으로 채움)
 * @RETURN 없음
 */
public record PopularCardDto(Long cardId, String rarity, double score, String imageUrl) {
}
