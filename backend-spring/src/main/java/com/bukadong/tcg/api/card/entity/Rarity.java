package com.bukadong.tcg.api.card.entity;

import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * 카드 희귀도 ENUM
 * <p>
 * A, B, C, DEFAULT 네 가지 희귀도를 표현한다.
 * </P>
 *
 * @RETURN 없음
 */
public enum Rarity {
    A, B, C, DEFAULT;

    public static Rarity getRarity(String rarity) {
        if (!StringUtils.hasText(rarity)) {
            return null; // 기본값은 DEFAULT 이나 Card 엔티티에서 처리함
        }
        try {
            return Rarity.valueOf(rarity.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BaseException(BaseResponseStatus.CARD_RARITY_UNSUPPORTED);
        }
    }
}
