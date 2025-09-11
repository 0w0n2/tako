package com.bukadong.tcg.api.admin.common.controller;

import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    // 간단한 헬스 체크 (ADMIN 전용)
    @GetMapping("/ping")
    public ResponseEntity<String> ping(@AuthenticationPrincipal CustomUserDetails user) {
        String who = (user != null) ? user.getUsername() : "unknown";
        return ResponseEntity.ok("admin pong - " + who);
    }
}
