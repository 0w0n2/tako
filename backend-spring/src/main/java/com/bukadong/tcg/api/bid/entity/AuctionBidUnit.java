package com.bukadong.tcg.api.bid.entity;

import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;

import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 입찰 단위 ENUM - DB에는 문자열
 * '0.0001','0.0005','0.001','0.005','0.01','0.05','0.1','0.5','1','5','10','50','100','500','1000','5000'로
 * 저장됨. - S115 규칙을 지키기 위해 문자로 시작하는 상수명 사용.
 */
public enum AuctionBidUnit {
    UNIT_0_0001("0.0001"), UNIT_0_0005("0.0005"), UNIT_0_001("0.001"), UNIT_0_005("0.005"), UNIT_0_01("0.01"),
    UNIT_0_05("0.05"), UNIT_0_1("0.1"), UNIT_0_5("0.5"), UNIT_1("1"), UNIT_5("5"), UNIT_10("10"), UNIT_50("50"),
    UNIT_100("100"), UNIT_500("500"), UNIT_1000("1000"), UNIT_5000("5000");

    private final String value;

    AuctionBidUnit(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /** value → Enum 상수 매핑 캐시 */
    private static final Map<String, AuctionBidUnit> CACHE = Stream.of(values())
            .collect(Collectors.toMap(AuctionBidUnit::value, e -> e));

    /**
     * DB 문자열 → Enum
     *
     * @param v DB에 저장된 문자열 값
     * @return 매칭되는 Enum
     * @throws BaseException 매핑 실패 시 BAD_REQUEST
     */
    public static AuctionBidUnit fromValue(String v) {
        AuctionBidUnit unit = CACHE.get(v);
        if (unit == null) {
            // 공통 예외/응답 체계를 사용해 명확한 에러로 전파
            throw new BaseException(BaseResponseStatus.INVALID_AUCTION_BID_UNIT);
        }
        return unit;
    }

    /**
     * Enum → BigDecimal
     *
     * @return BigDecimal 변환값
     */
    public BigDecimal toBigDecimal() {
        return new BigDecimal(this.value);
    }
}
