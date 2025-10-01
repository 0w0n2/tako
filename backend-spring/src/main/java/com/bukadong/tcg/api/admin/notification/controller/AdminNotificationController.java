package com.bukadong.tcg.api.admin.notification.controller;

import com.bukadong.tcg.api.notification.sse.UserNotificationSseService;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/notifications")
@Tag(name = "Admin")
public class AdminNotificationController {

    private final UserNotificationSseService sseService;

    /**
     * 개발 편의용: 관리자 컨텍스트에서 현재 로그인 유저에게 즉시 알림 이벤트 발송
     */
    @Operation(summary = "테스트용 알림 전송 (Admin)", description = "관리자용 테스트 API입니다. 로그인 유저에게 즉시 알림 이벤트를 전송합니다.")
    @PostMapping("/test")
    public Map<String, Object> test(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "알림 유형") @RequestParam(name = "type", defaultValue = "test") String type,
            @Parameter(description = "알림 메시지") @RequestParam(name = "message", defaultValue = "hello") String message) {
        sseService.sendToUser(user.getUuid(), type, Map.of("message", message));
        return Map.of("ok", true, "connections", sseService.getActiveConnectionCount(user.getUuid()));
    }
}
