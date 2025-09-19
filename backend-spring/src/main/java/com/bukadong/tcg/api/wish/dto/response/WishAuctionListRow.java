package com.bukadong.tcg.api.wish.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 위시 경매 목록 행 DTO
 * <P>
 * 관심 등록한 경매의 요약 정보(대표 이미지 키 포함)를 담는다. 이미지 키는 서비스에서 URL로 변환.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "WishAuctionListRow", description = "위시 경매 목록 행")
public class WishAuctionListRow {

    @Schema(description = "경매 ID", example = "123")
    private Long auctionId;

    @Schema(description = "대표 이미지 파일 키(SEQ=1)", example = "auction/item/123/main.jpg")
    private String imageKey;

    @Schema(description = "경매 제목", example = "전설의 푸른 용 1st Edition")
    private String title;

    @Schema(description = "현재 입찰가", example = "10.50000000")
    private BigDecimal currentPrice;

    @Schema(description = "종료 시간(ISO-8601)", example = "2025-09-30T23:59:59")
    private LocalDateTime endDatetime;
}
