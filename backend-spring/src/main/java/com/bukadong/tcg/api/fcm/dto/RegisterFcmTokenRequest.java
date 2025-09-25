package com.bukadong.tcg.api.fcm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RegisterFcmTokenRequest {
    @NotBlank(message = "FCM 토큰은 필수입니다.")
    private String token;
}
