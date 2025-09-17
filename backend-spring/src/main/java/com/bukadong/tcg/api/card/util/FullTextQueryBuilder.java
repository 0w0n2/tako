package com.bukadong.tcg.api.card.util;

/**
 * FULLTEXT BOOLEAN MODE 질의 문자열 빌더
 * <P>
 * 공백 분리 후 +토큰* AND 검색.
 * </P>
 * 
 * @PARAM keyword 사용자 키워드
 * @RETURN boolean 쿼리 문자열
 */
public final class FullTextQueryBuilder {

    private FullTextQueryBuilder() {
    }

    public static String buildAndPrefixQuery(String keyword) {
        return java.util.Arrays.stream(keyword.trim().split("\\s+")).filter(s -> !s.isBlank()).map(s -> "+" + s + "*")
                .collect(java.util.stream.Collectors.joining(" "));
    }
}
