package com.bukadong.tcg.api.fcm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RegisterFcmTokenRequest {
    @NotBlank
    private String token;
}
