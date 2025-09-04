package com.bukadong.tcg.member.service;

import com.bukadong.tcg.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 회원가입 시 이메일/닉네임 사용 가능 여부를 판단하는 서비스.
 *
 * <p>
 * Repository를 통해 DB에 동일한 값이 존재하는지 확인하고,
 * 대소문자 구분 없이 중복 여부를 검사한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAvailabilityService {

    /** null/공백만 문자열 방지 + 앞뒤 공백 제거 */
    private String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        // 앞뒤 공백만 정리 (내용 중간 공백은 유지)
        return value.strip();
    }

    private final MemberRepository memberRepository;

    /**
     * 이메일 사용 가능 여부를 확인한다.
     *
     * @param email 확인할 이메일
     * @return true = 사용 가능, false = 이미 존재
     * @throws IllegalArgumentException email 값이 null 또는 공백일 경우
     */
    public boolean isEmailAvailable(String email) {
        String v = requireText(email, "email");
        return !memberRepository.existsByEmail(v);
    }

    /**
     * 닉네임 사용 가능 여부를 확인한다.
     *
     * @param nickname 확인할 닉네임
     * @return true = 사용 가능, false = 이미 존재
     * @throws IllegalArgumentException nickname 값이 null 또는 공백일 경우
     */
    public boolean isNicknameAvailable(String nickname) {
        String v = requireText(nickname, "nickname");
        return !memberRepository.existsByNickname(v);
    }

}
