package com.bukadong.tcg.api.fcm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SendTestPushRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String body;
}
