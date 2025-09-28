package com.bukadong.tcg.api.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String type; // 알림 유형
    private String title; // 알림 타이틀
    private String message; // 본문
    private Long causeId; // 원인 리소스 ID(경매/카드/문의 등) - 프론트에서 type+causeId로 라우팅
}
