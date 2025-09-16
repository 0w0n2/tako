package com.bukadong.tcg.api.auth.controller;

import com.bukadong.tcg.api.auth.dto.request.EmailCodeConfirmRequestDto;
import com.bukadong.tcg.api.auth.dto.request.EmailCodeRequestDto;
import com.bukadong.tcg.api.auth.dto.response.EmailCodeConfirmResponseDto;
import com.bukadong.tcg.api.auth.dto.response.EmailCodeResponseDto;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.mail.dto.MailContext;
import com.bukadong.tcg.global.mail.dto.MailType;
import com.bukadong.tcg.global.mail.dto.VerificationCode;
import com.bukadong.tcg.global.mail.service.MailCodeVerificationService;
import com.bukadong.tcg.global.mail.service.MailSendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/auth/email/")
@Tag(name = "Auth", description = "사용자 인증/인가 관련 API")
@RequiredArgsConstructor
@RestController
public class AuthEmailController {

    private final MailCodeVerificationService mailCodeVerificationService;
    private final MailSendService mailSendService;

    @Operation(summary = "이메일 인증코드 발급/전송 API",
            description = "회원가입/분실 비밀번호 재설정 공용 사용")
    @PostMapping("/verification")
    public BaseResponse<EmailCodeResponseDto> sendEmailWithCode(@Valid @RequestBody EmailCodeRequestDto requestDto) {
        MailType mailType = MailType.getMailType(requestDto.verificationType());
        VerificationCode code = mailCodeVerificationService.generateVerificationCode(requestDto.email(), MailType.getMailType(requestDto.verificationType()));
        MailContext context = new MailContext().withVerificationCode(code);
        mailSendService.sendMail(requestDto.email(), mailType, context);

        return BaseResponse.onSuccess(EmailCodeResponseDto.toDto(code));
    }

    @Operation(summary = "이메일 인증코드 검증 API",
            description = "회원가입/분실 비밀번호 재설정 공용 사용")
    @PostMapping("/verification/confirm")
    public BaseResponse<EmailCodeConfirmResponseDto> confirmEmailCode(@Valid @RequestBody EmailCodeConfirmRequestDto requestDto) {
        return BaseResponse.onSuccess(mailCodeVerificationService.verifyEmailCode(requestDto));
    }
}
