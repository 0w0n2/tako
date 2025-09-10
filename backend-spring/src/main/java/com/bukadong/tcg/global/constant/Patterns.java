package com.bukadong.tcg.global.constant;

import java.util.regex.Pattern;

public final class Patterns {
    private Patterns() {
    }

    // 이메일 형식 검증: 매우 느슨한 이메일 형식 검증(실무에서는 더 엄격한 정책/화이트리스트 필요할 수 있음)
    public static final Pattern SIMPLE_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    // 닉네임 형식 검증: 영문/한글/숫자만 허용, 길이 2~30
    public static final Pattern NICKNAME_PATTERN = Pattern.compile("^[0-9A-Za-z가-힣]{2,30}$");
    // 비밀번호 형식 검증: 11~21자, 최소 하나의 대문자, 소문자, 숫자, 특수문자(/!@$~) 포함
    public static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[\\/!@$~])[A-Za-z\\d\\/!@$~]{11,21}$");
}
