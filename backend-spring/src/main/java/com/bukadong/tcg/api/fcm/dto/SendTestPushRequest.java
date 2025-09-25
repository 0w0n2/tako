package com.bukadong.tcg.api.fcm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SendTestPushRequest {
    @NotBlank(message = "푸시 알림 제목은 필수입니다.")
    private String title;
    @NotBlank(message = "푸시 알림 내용은 필수입니다.")
    private String body;
}
