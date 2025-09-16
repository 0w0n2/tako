package com.bukadong.tcg.api.member.service;

import com.bukadong.tcg.api.auth.dto.request.PasswordResetRequestDto;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import com.bukadong.tcg.global.mail.constants.MailConstants;
import com.bukadong.tcg.global.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 조회 서비스
 * <p>
 * uuid를 기반으로 회원 ID/엔티티를 조회한다.
 * </P>
 *
 * @PARAM uuid 회원 고유 식별자
 * @RETURN Member 또는 memberId
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RedisUtils redisUtils;

    /**
     * uuid로 Member 조회
     * <p>
     * 존재하지 않으면 NOT_FOUND 예외
     * </P>
     */
    public Member getByUuid(String uuid) {
        return memberRepository.findByUuid(uuid).orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
    }

    /**
     * 잃어버린 비밀번호 재설정
     */
    @Transactional
    public void updatePasswordWithResetCode(PasswordResetRequestDto requestDto) {
        verifyPasswordResetCode(requestDto.email(), requestDto.passwordResetCode());
        updatePassword(requestDto.email(), requestDto.password());
    }

    private void updatePassword(String email, String newPassword) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_USER));

        String encodedPassword = bCryptPasswordEncoder.encode(newPassword);

        member.updatePassword(encodedPassword);
    }

    private void verifyPasswordResetCode(String email, String resetCode) {
        String redisKey = MailConstants.PASSWORD_RESET_PREFIX + email;
        Object redisCode = redisUtils.getValue(redisKey);

        if (redisCode == null) {
            throw new BaseException(BaseResponseStatus.PASSWORD_RESET_CODE_EXPIRED);
        }

        if (!redisCode.toString().equals(resetCode)) {
            throw new BaseException(BaseResponseStatus.PASSWORD_RESET_CODE_MISMATCH);
        }

        redisUtils.deleteValue(redisKey);
    }
}
