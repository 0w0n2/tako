package com.bukadong.tcg.api.auction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 입찰 생성 요청
 * <P>
 * 요청가(bidPrice)만 전달. 통화는 서버 통일(BigDecimal) 기준.
 * </P>
 * 
 * @PARAM bidPrice 입찰가(현재가+입찰단위 이상)
 * @RETURN 없음
 */
public class BidCreateRequest {

    @Schema(description = "입찰가(현재가+입찰단위 이상)", example = "12000.00", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal bidPrice;

    public BigDecimal getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(BigDecimal bidPrice) {
        this.bidPrice = bidPrice;
    }
}
