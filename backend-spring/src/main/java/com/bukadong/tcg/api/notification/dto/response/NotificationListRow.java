package com.bukadong.tcg.api.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 알림 목록 행 DTO
 * <P>
 * 알림 리스트에서 단일 행의 표시 데이터를 전달한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Getter
@AllArgsConstructor
public class NotificationListRow {

    @Schema(description = "알림 ID")
    private Long id;

    @Schema(description = "알림 타입 코드", example = "WISH_AUCTION_STARTED")
    private String type;

    @Schema(description = "원인 리소스 ID(경매/카드/문의 등)", example = "123")
    private Long causeId;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "본문")
    private String message;

    @Schema(description = "대상 URL(현재는 빈 문자열)", example = "")
    private String targetUrl;

    @Schema(description = "읽음 여부", example = "false")
    private boolean read;

    @Schema(description = "읽은 시각(읽지 않았으면 null)")
    private LocalDateTime readAt;

    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;
}
