package com.bukadong.tcg.auction.converter;

import com.bukadong.tcg.auction.entity.AuctionBidUnit;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA {@link AttributeConverter} 구현체.
 *
 * <p>
 * 경매 입찰 단위({@link AuctionBidUnit}) Enum과
 * DB 문자열 컬럼 값(예: "0.01", "0.1", "1") 간의 변환을 처리한다.
 * </p>
 *
 * <ul>
 * <li>엔티티 → DB 저장 시: {@link AuctionBidUnit#value()} 반환</li>
 * <li>DB → 엔티티 로드 시: {@link AuctionBidUnit#fromValue(String)} 호출</li>
 * </ul>
 *
 * <p>
 * {@code @Converter(autoApply = true)} 설정으로
 * JPA가 해당 Enum 타입 필드를 자동으로 변환한다.
 * </p>
 */
@Converter(autoApply = true)
public class AuctionBidUnitConverter implements AttributeConverter<AuctionBidUnit, String> {

    /**
     * 엔티티의 {@link AuctionBidUnit} Enum을 DB 문자열 값으로 변환한다.
     *
     * @param attribute 엔티티 속성 값 (Enum)
     * @return DB에 저장될 문자열 값, null 허용
     */
    @Override
    public String convertToDatabaseColumn(AuctionBidUnit attribute) {
        return attribute == null ? null : attribute.value();
    }

    /**
     * DB 문자열 값을 {@link AuctionBidUnit} Enum으로 변환한다.
     *
     * @param dbData DB에 저장된 문자열 값
     * @return Enum 매핑 값, null 허용
     * @throws IllegalArgumentException 매핑되지 않는 값일 경우
     */
    @Override
    public AuctionBidUnit convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AuctionBidUnit.fromValue(dbData);
    }
}
