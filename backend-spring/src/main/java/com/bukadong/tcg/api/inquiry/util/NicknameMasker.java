package com.bukadong.tcg.api.inquiry.util;

/**
 * 닉네임 마스킹 유틸
 * <P>
 * 앞 1~2글자 노출, 나머지 * 처리.
 * </P>
 * 
 * @PARAM nickname 원문
 * @RETURN 마스킹 문자열
 */
public final class NicknameMasker {
    private NicknameMasker() {
    }

    public static String mask(String nickname) {
        if (nickname == null || nickname.isBlank())
            return "***";
        String s = nickname.trim();
        if (s.length() <= 2)
            return s.charAt(0) + "*";
        return s.substring(0, 2) + "*".repeat(Math.max(1, s.length() - 2));
    }
}
