package com.bukadong.tcg.global.mail.service;

import com.bukadong.tcg.api.auth.dto.request.EmailCodeConfirmRequestDto;
import com.bukadong.tcg.api.auth.dto.response.EmailCodeConfirmResponseDto;
import com.bukadong.tcg.global.mail.dto.MailType;
import com.bukadong.tcg.global.mail.dto.VerificationCode;

public interface MailCodeVerificationService {
    VerificationCode generateVerificationCode(String email, MailType mailType);

    EmailCodeConfirmResponseDto verifyEmailCode(EmailCodeConfirmRequestDto requestDto);
}
