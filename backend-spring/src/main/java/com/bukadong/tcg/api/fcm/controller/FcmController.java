package com.bukadong.tcg.api.fcm.controller;

import com.bukadong.tcg.api.fcm.dto.RegisterFcmTokenRequest;
import com.bukadong.tcg.api.fcm.dto.SendTestPushRequest;
import com.bukadong.tcg.api.fcm.service.FcmPushService;
import com.bukadong.tcg.api.fcm.service.FcmTokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final FcmPushService fcmPushService;

    /**
     * 토큰 등록 (임시: memberId QueryParam 로 전달)
     */
    @PostMapping("/token")
    @Operation(summary = "FCM 토큰 등록", description = "회원 ID와 FCM 토큰을 전달받아 등록합니다. 이미 등록된 토큰인 경우 무시합니다.")
    public ResponseEntity<Void> register(
            @Parameter(description = "회원 ID", required = true) @RequestParam("memberId") Long memberId,
            @Valid @RequestBody RegisterFcmTokenRequest req) {
        fcmTokenService.register(memberId, req.getToken());
        return ResponseEntity.ok().build();
    }

    /**
     * 이 기기만 활성화(재설정) - 회원 기존 모든 토큰 제거 후 새 토큰 등록
     */
    @PostMapping("/token/reset")
    @Operation(summary = "FCM 토큰 재설정", description = "회원 ID와 새로운 FCM 토큰을 전달받아 해당 회원의 기존 모든 토큰을 제거하고 새 토큰 하나만 등록합니다.")
    public ResponseEntity<Void> resetSingle(
            @Parameter(description = "회원 ID", required = true) @RequestParam("memberId") Long memberId,
            @Valid @RequestBody RegisterFcmTokenRequest req) {
        fcmTokenService.resetSingleDevice(memberId, req.getToken());
        return ResponseEntity.ok().build();
    }

    /**
     * FCM 활성 상태 조회
     * 
     * @param memberId 회원 ID (임시) / currentToken (선택) : 현재 브라우저가 가진 토큰이 등록되어 있는지 확인용
     */
    @GetMapping("/status")
    @Operation(summary = "FCM 상태 조회", description = "회원 ID와 선택적으로 현재 토큰을 전달받아 FCM 활성 상태를 조회합니다.")
    public ResponseEntity<FcmTokenService.FcmStatus> status(
            @Parameter(description = "회원 ID", required = true) @RequestParam("memberId") Long memberId,
            @Parameter(description = "현재 FCM 토큰") @RequestParam(name = "currentToken", required = false) String currentToken) {
        return ResponseEntity.ok(fcmTokenService.status(memberId, currentToken));
    }

    /**
     * 특정 회원에게 테스트 푸시 (임시)
     */
    @PostMapping("/test/{memberId}")
    @Operation(summary = "회원 대상 테스트 푸시 전송", description = "회원 ID와 푸시 제목 및 내용을 전달받아 해당 회원에게 등록된 모든 기기로 테스트 푸시를 전송합니다.")
    public ResponseEntity<String> sendTest(
            @Parameter(description = "회원 ID", required = true) @PathVariable("memberId") Long memberId,
            @Valid @RequestBody SendTestPushRequest req) {
        int success = fcmPushService.sendToMember(memberId, req.getTitle(), req.getBody());
        return ResponseEntity.ok("sent=" + success);
    }

    /**
     * Raw FCM registration token 으로 직접 테스트 (회원 관계 없이 단건) Swagger에서 실제 기기 토큰 붙여 전송
     * 확인용.
     */
    @PostMapping("/test/raw")
    @Operation(summary = "Raw FCM 토큰 테스트 푸시 전송", description = "회원 관계 없이 특정 FCM 토큰으로 테스트 푸시를 전송합니다. Swagger에서 실제 기기 토큰을 전달하여 테스트할 수 있습니다.")
    public ResponseEntity<String> sendRaw(
            @Parameter(description = "FCM 토큰", required = true) @RequestParam("token") String token,
            @Valid @RequestBody SendTestPushRequest req) {
        int success = fcmPushService.sendRawToken(token, req.getTitle(), req.getBody()) ? 1 : 0;
        return ResponseEntity.ok("sent=" + success);
    }
}
