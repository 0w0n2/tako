package com.bukadong.tcg.api.auction.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 경매 목록 조회 내부 행 DTO
 * <P>
 * QueryDSL 프로젝션 결과를 담는 내부용 DTO로, 서비스 레이어에서 remainingSeconds 계산 후
 * {@link AuctionListItemDto}로 변환한다.
 * </P>
 *
 * @param id              경매 ID
 * @param grade           카드 등급
 * @param title           경매 제목
 * @param currentPrice    현재가
 * @param bidCount        유효 입찰 수(VALID)
 * @param endDatetime     마감 일시
 * @param primaryImageUrl 대표 이미지 URL
 * @return 내부 행 DTO
 */
public record AuctionListRow(Long id, String grade, String title, BigDecimal currentPrice,
        long bidCount, LocalDateTime endDatetime, String primaryImageUrl) {
}