package com.bukadong.tcg.api.popularity.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 인기 랭킹 Redis 키 유틸
 * <P>
 * 카테고리/분 버킷 키 및 멤버 인코딩을 담당한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public final class PopularityKeyUtil {
    private static final DateTimeFormatter MIN_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private PopularityKeyUtil() {
    }

    /**
     * 분 버킷 ZSET 키 생성
     * <P>
     * 형식: pop:cat:{categoryId}:min:{yyyyMMddHHmm}
     * </P>
     * 
     * @PARAM categoryId 카테고리 ID
     * @PARAM when 기준 시각
     * @RETURN Redis 키
     */
    public static String minuteKey(long categoryId, LocalDateTime when) {
        return "pop:cat:" + categoryId + ":min:" + when.atZone(ZoneOffset.UTC).format(MIN_FMT);
    }

    /**
     * 임시 결과 키 생성
     * <P>
     * unionAndStore 결과를 보관하기 위한 임시 키.
     * </P>
     * 
     * @PARAM categoryId 카테고리 ID
     * @PARAM nonce 임의 문자열
     * @RETURN 임시 키
     */
    public static String tempKey(long categoryId, String nonce) {
        return "pop:cat:" + categoryId + ":tmp:" + nonce;
    }
}
