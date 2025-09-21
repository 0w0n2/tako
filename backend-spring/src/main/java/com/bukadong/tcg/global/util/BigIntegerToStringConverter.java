package com.bukadong.tcg.global.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigInteger;

import org.springframework.util.StringUtils;

@Converter
public class BigIntegerToStringConverter implements AttributeConverter<BigInteger, String> {

    @Override
    public String convertToDatabaseColumn(BigInteger attribute) {
        // BigInteger 객체를 DB에 저장할 때 문자열로 변환
        return (attribute == null) ? null : attribute.toString();
    }

    @Override
    public BigInteger convertToEntityAttribute(String dbData) {
        // DB의 문자열을 다시 BigInteger 객체로 변환
        return (StringUtils.hasText(dbData)) ? new BigInteger(dbData) : null;
    }
}
