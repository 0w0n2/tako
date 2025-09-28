package com.bukadong.tcg.api.card.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * MySQL ngram FULLTEXT용 BOOLEAN MODE 질의 빌더
 * <P>
 * - CJK(한중일) 토큰은 n-gram으로 슬라이딩(+토큰) AND 조건 - 비CJK 토큰은 "+word*" (접두 매칭) - 토큰 없으면
 * null 반환(호출 측에서 LIKE fallback)
 * </P>
 * 
 * @PARAM keyword 사용자 입력
 * @RETURN BOOLEAN MODE 질의 문자열 또는 null
 */
public final class FullTextBooleanQuery {

    private static final Pattern CJK = Pattern.compile("[\\p{IsHangul}\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}]");
    private static final Pattern BAD = Pattern.compile("[+\\-@~<>()\"*]"); // boolean mode 예약문자

    private FullTextBooleanQuery() {
    }

    public static String buildForMySQLNgram(String keyword, int ngram) {
        if (keyword == null)
            return null;
        String trimmed = keyword.trim();
        if (trimmed.isEmpty())
            return null;

        List<String> tokens = new ArrayList<>();
        for (String part : trimmed.split("\\s+")) {
            if (part.isBlank())
                continue;
            if (isCjk(part)) {
                String s = part.replaceAll("\\s+", "");
                List<String> grams = toNgrams(s, ngram);
                if (grams.isEmpty())
                    continue;
                tokens.addAll(grams.stream().map(g -> "+" + g).collect(Collectors.toList()));
            } else {
                String cleaned = BAD.matcher(part).replaceAll("");
                if (!cleaned.isEmpty())
                    tokens.add("+" + cleaned + "*");
            }
        }
        return tokens.isEmpty() ? null : String.join(" ", tokens);
    }

    private static boolean isCjk(String s) {
        return CJK.matcher(s).find();
    }

    private static List<String> toNgrams(String s, int n) {
        List<String> out = new ArrayList<>();
        if (s.length() < n)
            return out;
        for (int i = 0; i <= s.length() - n; i++)
            out.add(s.substring(i, i + n));
        return out;
    }
}
