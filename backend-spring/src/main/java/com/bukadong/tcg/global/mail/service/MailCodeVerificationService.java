package com.bukadong.tcg.global.mail.service;

import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.global.mail.dto.VerificationCode;
import com.bukadong.tcg.global.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.bukadong.tcg.global.constant.MailConstants.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailCodeVerificationService {

    private final RedisUtils redisUtils;
    private final MemberRepository memberRepository;

    public VerificationCode generateVerificationCode(String email) {
        String code = UUID.randomUUID().toString().substring(0, 6);

        String redisKey = SIGNUP_CODE_PREFIX + email;
        redisUtils.setValue(redisKey, code, signUpMailCodeExpMin);  // value: signup-code:email, key: code(UUID)

        String expiredAt = LocalDateTime.now().plus(signUpMailCodeExpMin)
                .format(DateTimeFormatter.ISO_DATE_TIME);

        return VerificationCode.toDto(code, expiredAt);
    }

//    @Transactional(readOnly = true)
//    public VerifyEmailResponseDto verifyEmailCode(VerifyEmailRequestDto requestDto) {
//        String redisKey = SIGNUP_CODE_PREFIX + requestDto.getEmail();
//        Object redisCode = redisUtils.getValue(redisKey);
//        if (redisCode == null) { // redis 에 key 존재 X -> TTL 완료
//            return VerifyEmailResponseDto.toDto(false, true); // 만료 O
//        }
//        boolean isMatch = redisCode.toString().equals(requestDto.getCode());
//        return VerifyEmailResponseDto.toDto(isMatch, false);
//    }
//
//    @Transactional(readOnly = true)
//    public CheckEmailResponseDto checkEmail(CheckEmailRequestDto requestDto) {
//        return CheckEmailResponseDto.toDto(memberRepository.existsByEmailAndIsDeletedIsFalse(requestDto.getEmail()));
//    }

}
