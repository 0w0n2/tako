package com.bukadong.tcg.api.bid.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 큐 기반 입찰 요청
 * <P>
 * 비동기/최종일관성 경로. 멱등키(requestId) 필수.
 * </P>
 * 
 * @PARAM bidPrice 입찰가
 * @PARAM requestId 멱등키(Idempotency-Key). 클라이언트가 UUID 등으로 생성.
 * @RETURN 없음
 */
public class BidQueueRequest {

    @Schema(description = "입찰가(현재가+입찰단위 이상)", example = "12000.00000000")
    @NotNull(message = "입찰가는 필수입니다.")
    @DecimalMin("0.00000000")
    private BigDecimal bidPrice;

    @Schema(description = "멱등키(요청 고유 ID)", example = "0f7ecf3a-b63c-43c6-9c38-8b1f3fe5f2a1")
    @NotBlank(message = "멱등키는 필수입니다.")
    private String requestId;

    public BigDecimal getBidPrice() {
        return bidPrice;
    }

    public String getRequestId() {
        return requestId;
    }
}
