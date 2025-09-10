package com.bukadong.tcg.api.auction.dto.response;

import java.math.BigDecimal;

/**
 * 경매 목록 조회 응답 DTO
 * <P>
 * 카드형 목록에 필요한 최소 필드만 포함한다.
 * </P>
 *
 * @param id               경매 ID
 * @param grade            카드 등급
 * @param title            경매 제목
 * @param currentPrice     현재가
 * @param bidCount         유효 입찰 수(VALID)
 * @param remainingSeconds 남은 시간(초)
 * @param primaryImageUrl  대표 이미지 URL(없으면 null)
 * @return 조회 전용 DTO
 */
public record AuctionListItemDto(Long id, String grade, String title, BigDecimal currentPrice,
        long bidCount, long remainingSeconds, String primaryImageUrl) {
}
