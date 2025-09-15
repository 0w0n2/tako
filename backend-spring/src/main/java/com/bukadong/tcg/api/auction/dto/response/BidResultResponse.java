package com.bukadong.tcg.api.auction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 입찰 결과 응답
 * <P>
 * 생성된 입찰/반영된 현재가/상태를 반환한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 입찰 결과 정보
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BidResultResponse {

    @Schema(description = "입찰 ID", example = "50123")
    private Long bidId;

    @Schema(description = "경매 ID", example = "1001")
    private Long auctionId;

    @Schema(description = "반영된 현재가", example = "12000.00")
    private BigDecimal currentPrice;

    @Schema(description = "입찰 시각(Asia/Seoul)", example = "2025-09-15T16:12:30")
    private LocalDateTime bidAt;

    @Schema(description = "결과 상태 (ACCEPTED/REJECTED 등)", example = "ACCEPTED")
    private String status;

}
