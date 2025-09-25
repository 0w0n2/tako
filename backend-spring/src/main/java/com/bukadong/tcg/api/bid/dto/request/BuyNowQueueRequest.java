package com.bukadong.tcg.api.bid.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 즉시구매(큐) 요청
 * <p>
 * 금액은 경매의 buy_now_price로 강제되므로 멱등키만 받습니다.
 * </p>
 */
public class BuyNowQueueRequest {

    @Schema(description = "멱등키(요청 고유 ID)", example = "0f7ecf3a-b63c-43c6-9c38-8b1f3fe5f2a1")
    @NotBlank(message = "멱등키는 필수입니다.")
    private String requestId;

    public String getRequestId() {
        return requestId;
    }
}
