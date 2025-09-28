package com.bukadong.tcg.api.card.entity;

import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * 카드 속성 (가위/바위/보)
 */
public enum CardAttribute {
    ROCK, PAPER, SCISSORS;

    public static CardAttribute getAttribute(String attribute) {
        if (!StringUtils.hasText(attribute)) {
            return null;
        }
        try {
            return CardAttribute.valueOf(attribute.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BaseException(BaseResponseStatus.CARD_ATTRIBUTE_UNSUPPORTED);
        }
    }
}
