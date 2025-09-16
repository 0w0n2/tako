package com.bukadong.tcg.api.auth.controller;

import com.bukadong.tcg.api.auth.dto.request.EmailVerificationRequestDto;
import com.bukadong.tcg.api.auth.dto.request.SignInRequestDto;
import com.bukadong.tcg.api.auth.dto.request.SignUpRequestDto;
import com.bukadong.tcg.api.auth.dto.response.RandomNicknameResponseDto;
import com.bukadong.tcg.api.auth.service.NicknameService;
import com.bukadong.tcg.api.auth.service.TokenAuthService;
import com.bukadong.tcg.api.auth.service.SignUpService;
import com.bukadong.tcg.global.common.base.BaseResponse;

import static com.bukadong.tcg.global.constant.SecurityConstants.*;

import com.bukadong.tcg.global.mail.dto.MailContext;
import com.bukadong.tcg.global.mail.dto.MailType;
import com.bukadong.tcg.global.mail.dto.VerificationCode;
import com.bukadong.tcg.global.mail.service.MailCodeVerificationService;
import com.bukadong.tcg.global.mail.service.MailSendService;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1/auth")
@Tag(name = "Auth", description = "사용자 인증/인가 관련 API")
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final TokenAuthService tokenAuthService;
    private final SignUpService signUpService;
    private final NicknameService nicknameService;
    private final MailCodeVerificationService mailCodeVerificationService;
    private final MailSendService mailSendService;

    @Operation(summary = "일반 로그인 API")
    @PostMapping("/sign-in")
    public BaseResponse<Void> signIn(@Valid @RequestBody SignInRequestDto requestDto, HttpServletResponse response) {
        CustomUserDetails userDetails = tokenAuthService.authenticate(requestDto.email(), requestDto.password());
        tokenAuthService.issueJwt(userDetails, response);
        return BaseResponse.onSuccess();
    }

    @Operation(summary = "일반/소셜 공통 회원가입 API",
            description = "현재 소셜 회원가입 로직 및 디폴트 프로필 이미지 할당은 구현되어 있지 않음")
    @PostMapping("/sign-up")
    public BaseResponse<Void> signUp(@Valid @RequestBody SignUpRequestDto requestDto, HttpServletResponse response) {
        signUpService.signUp(requestDto);
        return BaseResponse.onSuccess();
    }

    @Operation(summary = "로그아웃 API")
    @PostMapping("/sign-out")
    public BaseResponse<Void> signOut(HttpServletRequest request, HttpServletResponse response) {
        tokenAuthService.signOut(request, response);
        return BaseResponse.onSuccess();
    }

    @Operation(summary = "Access Token 재발급 API")
    @PostMapping("/token/refresh")
    public BaseResponse<Void> refresh(@CookieValue(value = REFRESH_TITLE) String refreshToken, HttpServletResponse response) {
        tokenAuthService.refreshAccessToken(response, refreshToken);
        return BaseResponse.onSuccess();
    }

    @Operation(summary = "랜덤 닉네임 생성 및 조회 API")
    @GetMapping("/random-nickname")
    public BaseResponse<RandomNicknameResponseDto> randomNickname() {
        return BaseResponse.onSuccess(RandomNicknameResponseDto.toDto(nicknameService.getRandomNickname()));
    }

    @Operation(summary = "이메일 인증코드 발급/전송 API",
            description = "회원가입/분실 비밀번호 재설정 공용 사용")
    @PostMapping("/email/verification")
    public BaseResponse<?> sendEmailWithCode(EmailVerificationRequestDto requestDto) {
        MailType mailType = MailType.getMailType(requestDto.verificationType());
        VerificationCode code = mailCodeVerificationService.generateVerificationCode(requestDto.email());
        MailContext context = new MailContext().withVerificationCode(code);
        mailSendService.sendMail(requestDto.email(), mailType, context);

        return BaseResponse.onSuccess();
    }
}
