package com.bukadong.tcg.api.auction.dto.projection;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 경매 목록 조회 프로젝션 DTO
 * <p>
 * QueryDSL 프로젝션 결과를 담는 **내부 전용** DTO입니다. 서비스 레이어에서 남은 시간 계산 및 이미지 key → presign
 * URL 변환 후 외부 응답 DTO(AuctionListItemResponse)로 매핑합니다.
 * </P>
 *
 * @PARAM id 경매 ID
 * @PARAM grade 카드 등급
 * @PARAM title 경매 제목
 * @PARAM currentPrice 현재가
 * @PARAM bidCount 유효 입찰 수(VALID)
 * @PARAM endDatetime 마감 일시
 * @PARAM primaryImageKey 대표 이미지 S3 key(후처리에서 presign)
 * @RETURN 내부 프로젝션 DTO
 */
public record AuctionListProjection(Long id, String grade, String title, BigDecimal currentPrice, long bidCount,
                                    LocalDateTime endDatetime, String primaryImageKey, BigInteger tokenId) {
}
