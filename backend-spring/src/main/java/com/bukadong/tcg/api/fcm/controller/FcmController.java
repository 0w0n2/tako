package com.bukadong.tcg.api.fcm.controller;

import com.bukadong.tcg.api.fcm.dto.RegisterFcmTokenRequest;
import com.bukadong.tcg.api.fcm.service.FcmTokenService;
import com.bukadong.tcg.api.member.service.MemberQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;

/**
 * FCM 토큰 등록 & 임시 테스트 발송 API
 * <p>
 * 실제 서비스 도입 전 임시/개발용. 추후 Security 인증 컨텍스트에서 memberId 추출하도록 수정 가능.
 * </p>
 */
@RestController
@Tag(name = "FCM", description = "Firebase Cloud Messaging API")
@RequestMapping("/v1/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmTokenService fcmTokenService;
    private final MemberQueryService memberQueryService;

    @PostMapping("/enable")
    @Operation(summary = "FCM 활성화(토큰 등록)", description = "현재 로그인한 사용자에 대해 전달된 FCM 토큰을 등록합니다.")
    public ResponseEntity<Void> enable(@AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody RegisterFcmTokenRequest req) {
        Long memberId = memberQueryService.getByUuid(user.getUuid()).getId();
        fcmTokenService.register(memberId, req.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/disable")
    @Operation(summary = "FCM 비활성화(토큰 해제)", description = "현재 로그인한 사용자의 전달된 FCM 토큰을 해제(삭제)합니다.")
    public ResponseEntity<Void> disable(@AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody RegisterFcmTokenRequest req) {
        Long memberId = memberQueryService.getByUuid(user.getUuid()).getId();
        fcmTokenService.unregister(memberId, req.getToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    @Operation(summary = "FCM 상태 조회", description = "현재 로그인한 사용자의 FCM 등록 상태를 조회합니다. token 파라미터를 주면 해당 토큰 등록 여부도 함께 반환합니다.")
    public ResponseEntity<FcmTokenService.FcmStatus> status(@AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "확인할 FCM 토큰") @RequestParam(name = "token", required = false) String token) {
        Long memberId = memberQueryService.getByUuid(user.getUuid()).getId();
        return ResponseEntity.ok(fcmTokenService.status(memberId, token));
    }
}
