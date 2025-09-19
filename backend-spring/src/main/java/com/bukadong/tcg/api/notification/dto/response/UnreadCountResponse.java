package com.bukadong.tcg.api.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 미읽음 카운트 응답
 * <P>
 * 현재 사용자 기준 미읽은 알림 개수.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Getter
@AllArgsConstructor
public class UnreadCountResponse {

    @Schema(description = "미읽음 개수", example = "3")
    private long unreadCount;
}
