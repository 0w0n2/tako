package com.bukadong.tcg.api.notification.controller;

import com.bukadong.tcg.api.notification.sse.UserNotificationSseService;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Notifications")
@RequestMapping("/v1/notifications")
public class UserNotificationController {

    private final UserNotificationSseService sseService;

    /**
     * 유저별 SSE 구독. Authorization 헤더로만 인증.
     */
    @Operation(summary = "SSE 구독", description = "로그인 유저의 알림 수신을 위한 SSE 구독을 시작합니다. Authorization 헤더(Bearer)로 인증합니다.")
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal CustomUserDetails user) {
        String uuid = user.getUuid();
        return sseService.subscribe(uuid);
    }

    /**
     * 개발 편의용 테스트 API: 로그인 유저에게 즉시 알림 이벤트 발송
     */
    @Operation(summary = "테스트용 알림 전송", description = "개발 편의용 테스트 API입니다. 로그인 유저에게 즉시 알림 이벤트를 전송합니다.")
    @PostMapping("/test")
    public Map<String, Object> test(@AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "알림 유형") @RequestParam(name = "type", defaultValue = "test") String type,
            @Parameter(description = "알림 메시지") @RequestParam(name = "message", defaultValue = "hello") String message) {
        sseService.sendToUser(user.getUuid(), type, Map.of("message", message));
        return Map.of("ok", true, "connections", sseService.getActiveConnectionCount(user.getUuid()));
    }
}
