package com.bukadong.tcg.api.auction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 경매 목록 단건 응답 DTO
 * <p>
 * 클라이언트에 반환되는 최종 형태. 이미지 URL은 presign 적용된 최종 URL이다.
 * </P>
 *
 * @PARAM id 경매 ID
 * @PARAM grade 카드 등급
 * @PARAM title 경매 제목
 * @PARAM currentPrice 현재가
 * @PARAM bidCount 유효 입찰 수
 * @PARAM remainingSeconds 마감까지 남은 초(0 이상)
 * @PARAM primaryImageUrl 대표 이미지 프리사인 URL(없으면 null)
 * @RETURN 단건 응답 DTO
 */
public record AuctionListItemResponse(@Schema(description = "경매 ID") Long id,
        @Schema(description = "카드 등급") String grade, @Schema(description = "경매 제목") String title,
        @Schema(description = "현재가") BigDecimal currentPrice, @Schema(description = "유효 입찰 수") long bidCount,
        @Schema(description = "마감까지 남은 초") long remainingSeconds,
        @Schema(description = "대표 이미지 프리사인 URL") String primaryImageUrl,
        @Schema(description = "로그인 회원이 위시했는지 여부") boolean wished,
        @Schema(description = "NFT 카드의 토큰 ID", example = "123455555") BigInteger tokenId,
        @Schema(description = "경매 시작 일시") LocalDateTime startDatetime) {
}
