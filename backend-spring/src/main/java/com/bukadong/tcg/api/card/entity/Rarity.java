package com.bukadong.tcg.api.card.entity;

import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * 카드 희귀도 ENUM
 * <p>
 * </P>
 *
 * @RETURN 없음
 */
public enum Rarity {
    RADIANT_RARE,
    TRAINER_GALLERY_RARE_HOLO,
    RARE_HOLO_COSMOS,
    RARE_HOLO_V,
    UNCOMMON,
    RARE_HOLO_VSTAR,
    RARE_HOLO_VMAX,
    COMMON,
    RARE_HOLO,
    AMAZING_RARE,
    RARE_SHINY,
    RARE_RAINBOW,
    RARE_SECRET,
    RARE_ULTRA;

    public static Rarity getRarity(String rarity) {
        if (!StringUtils.hasText(rarity)) {
            return COMMON;
        }
        try {
            return Rarity.valueOf(rarity.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BaseException(BaseResponseStatus.CARD_RARITY_UNSUPPORTED);
        }
    }
}
