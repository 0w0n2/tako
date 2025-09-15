package com.bukadong.tcg.api.admin.common.controller;

import com.bukadong.tcg.global.security.dto.CustomUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin")
@Tag(name = "Admin")
public class AdminController {

    // 간단한 헬스 체크 (ADMIN 전용)
    @GetMapping("/ping")
    public ResponseEntity<String> ping(@AuthenticationPrincipal CustomUserDetails user) {
        String who = (user != null) ? user.getUsername() : "unknown";
        return ResponseEntity.ok("admin pong - " + who);
    }
}
