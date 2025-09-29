package com.bukadong.tcg.api.inquiry.util;

public class TitleTrimer {
    private TitleTrimer() {
    }

    public static String trimAsTitle(String content) {
        if (content == null)
            return "";
        int limit = 30;
        String s = content.replaceAll("\\s+", " ").trim();
        return s.length() > limit ? s.substring(0, limit) + "…" : s;
    }
}
