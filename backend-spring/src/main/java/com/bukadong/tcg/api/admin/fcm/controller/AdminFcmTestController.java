package com.bukadong.tcg.api.admin.fcm.controller;

import com.bukadong.tcg.api.fcm.dto.SendTestPushRequest;
import com.bukadong.tcg.api.fcm.service.FcmPushService;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 전용 FCM 테스트 발송 API
 * <p>
 * 기존 /v1/fcm/test/** 엔드포인트를 관리자 전용으로 분리.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/fcm")
@Tag(name = "Admin")
public class AdminFcmTestController {

    private final FcmPushService fcmPushService;

    @PostMapping("/test/member/{memberId}")
    @Operation(summary = "회원 대상 테스트 푸시", description = "지정한 회원 ID 의 모든 토큰으로 테스트 푸시를 전송합니다.")
    public ResponseEntity<String> sendToMember(
            @Parameter(description = "회원 ID", required = true) @PathVariable Long memberId,
            @Valid @RequestBody SendTestPushRequest req) {
        int success = fcmPushService.sendToMember(memberId, req.getTitle(), req.getBody());
        return ResponseEntity.ok("sent=" + success);
    }

    @PostMapping("/test/raw-token")
    @Operation(summary = "Raw 토큰 단건 테스트 푸시", description = "회원과 무관하게 특정 FCM registration token 으로 직접 전송합니다.")
    public ResponseEntity<String> sendRaw(
            @Parameter(description = "FCM registration token", required = true) @RequestParam("token") String token,
            @Valid @RequestBody SendTestPushRequest req) {
        int success = fcmPushService.sendRawToken(token, req.getTitle(), req.getBody()) ? 1 : 0;
        return ResponseEntity.ok("sent=" + success);
    }
}
