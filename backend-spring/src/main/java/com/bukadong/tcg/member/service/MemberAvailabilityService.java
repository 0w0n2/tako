package com.bukadong.tcg.member.service;

import com.bukadong.tcg.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final MemberRepository memberRepository;

    /**
     * 이메일 사용 가능 여부를 확인한다.
     *
     * @param email 확인할 이메일
     * @return true = 사용 가능, false = 이미 존재
     * @throws IllegalArgumentException email 값이 null 또는 공백일 경우
     */
    public boolean isEmailAvailable(String email) {
        String v = normalize(email);
        if (v.isEmpty()) {
            throw new IllegalArgumentException("email is required");
        }
        return !memberRepository.existsByEmailIgnoreCase(v);
    }

    /**
     * 닉네임 사용 가능 여부를 확인한다.
     *
     * @param nickname 확인할 닉네임
     * @return true = 사용 가능, false = 이미 존재
     * @throws IllegalArgumentException nickname 값이 null 또는 공백일 경우
     */
    public boolean isNicknameAvailable(String nickname) {
        String v = normalize(nickname);
        if (v.isEmpty()) {
            throw new IllegalArgumentException("nickname is required");
        }
        return !memberRepository.existsByNicknameIgnoreCase(v);
    }

    /**
     * 입력 문자열을 정규화한다.
     *
     * <ul>
     * <li>null → 빈 문자열("")</li>
     * <li>앞뒤 공백 제거(trim)</li>
     * </ul>
     *
     * @param s 입력 문자열
     * @return 정규화된 문자열
     */
    private String normalize(String s) {
        return s == null ? "" : s.trim();
    }
}
