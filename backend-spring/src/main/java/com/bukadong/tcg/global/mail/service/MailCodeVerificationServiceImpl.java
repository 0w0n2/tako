package com.bukadong.tcg.global.mail.service;

import com.bukadong.tcg.api.auth.dto.request.EmailCodeConfirmRequestDto;
import com.bukadong.tcg.api.auth.dto.response.EmailCodeConfirmResponseDto;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.mail.dto.MailType;
import com.bukadong.tcg.global.mail.dto.VerificationCode;
import com.bukadong.tcg.global.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.bukadong.tcg.global.mail.constants.MailConstants.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailCodeVerificationServiceImpl implements MailCodeVerificationService {

    private final RedisUtils redisUtils;
    private final MemberRepository memberRepository;

    @Override
    public VerificationCode generateVerificationCode(String email, MailType mailType) {
        if (Objects.requireNonNull(mailType) == MailType.PASSWORD_RESET_MAIL_VERIFICATION) {
            if (!memberRepository.existsByEmail(email)) {
                throw new BaseException(BaseResponseStatus.NO_EXIST_USER);
            }
        }

        String code = UUID.randomUUID().toString().substring(0, 6);

        String redisKey = MAIL_VERIFICATION_CODE_PREFIX + mailType + ":" + email;
        redisUtils.setValue(redisKey, code, mailCodeExpMin);  // value: signup-code:email, key: code(UUID)

        String expiredAt = LocalDateTime.now().plus(mailCodeExpMin)
                .format(DateTimeFormatter.ISO_DATE_TIME);

        return VerificationCode.toDto(code, expiredAt);
    }

    @Override
    public EmailCodeConfirmResponseDto verifyEmailCode(EmailCodeConfirmRequestDto requestDto) {
        MailType mailType = MailType.getMailType(requestDto.verificationType());

        String redisKey = MAIL_VERIFICATION_CODE_PREFIX + mailType + ":" + requestDto.email();
        Object redisCode = redisUtils.getValue(redisKey);
        if (redisCode == null) { // redis 에 key 존재 X -> TTL 완료
            return EmailCodeConfirmResponseDto.toDto(false, true); // 만료 O
        }
        boolean isMatch = redisCode.toString().equals(requestDto.code());
        // redisUtils.deleteValue(redisKey);   // 1회 검증 후엔 만료 처리 (필요 시 주석 해제)
        return EmailCodeConfirmResponseDto.toDto(isMatch, false);
    }

}
