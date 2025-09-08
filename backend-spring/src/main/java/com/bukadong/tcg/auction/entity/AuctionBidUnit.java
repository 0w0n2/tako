package com.bukadong.tcg.auction.entity;

import com.bukadong.tcg.common.exception.BaseException;
import com.bukadong.tcg.common.base.BaseResponseStatus;

import java.math.BigDecimal;

/**
 * 입찰 단위 ENUM
 * - DB에는 문자열
 * '0.01','0.1','0.3','0.5','1','5','10','50','100','500','1000','5000'로 저장됨.
 * - S115 규칙을 지키기 위해 문자로 시작하는 상수명 사용.
 */
public enum AuctionBidUnit {
    UNIT_0_01("0.01"),
    UNIT_0_05("0.05"),
    UNIT_0_1("0.1"),
    UNIT_0_5("0.5"),
    UNIT_1("1"),
    UNIT_5("5"),
    UNIT_10("10"),
    UNIT_50("50"),
    UNIT_100("100"),
    UNIT_500("500"),
    UNIT_1000("1000"),
    UNIT_5000("5000");

    private final String value;

    AuctionBidUnit(String v) {
        this.value = v;
    }

    /** DB 저장/전송용 원문 값 */
    public String value() {
        return value;
    }

    /** 산술 연산용 BigDecimal */
    public BigDecimal asBigDecimal() {
        return new BigDecimal(value);
    }

    /**
     * DB 문자열 → Enum
     *
     * @param v DB에 저장된 문자열 값
     * @return 매칭되는 Enum
     * @throws BaseException 매핑 실패 시 BAD_REQUEST
     */
    public static AuctionBidUnit fromValue(String v) {
        for (AuctionBidUnit u : values()) {
            if (u.value.equals(v)) {
                return u;
            }
        }
        // 공통 예외/응답 체계를 사용해 명확한 에러로 전파
        throw new BaseException(BaseResponseStatus.Auction_Bid_Unit_Converter_BAD_REQUEST);
    }
}
