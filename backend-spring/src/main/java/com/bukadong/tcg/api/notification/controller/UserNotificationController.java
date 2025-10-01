package com.bukadong.tcg.api.notification.controller;

import com.bukadong.tcg.api.notification.sse.UserNotificationSseService;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
}
