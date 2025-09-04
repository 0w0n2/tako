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

    /** 이메일 사용 가능 여부 */
    public boolean isEmailAvailable(String email) {
        String v = email == null ? "" : email.strip();
        return !memberRepository.existsByEmail(v);
    }

    /** 닉네임 사용 가능 여부 */
    public boolean isNicknameAvailable(String nickname) {
        String v = nickname == null ? "" : nickname.strip();
        return !memberRepository.existsByNickname(v);
    }

}
