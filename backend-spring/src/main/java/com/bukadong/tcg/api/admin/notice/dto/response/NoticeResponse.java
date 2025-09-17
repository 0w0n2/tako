package com.bukadong.tcg.api.admin.notice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import com.bukadong.tcg.api.notice.entity.Notice;

/**
 * 공지 응답 DTO
 * <P>
 * 관리자/유저 공용으로 노출 가능한 공지 메타를 제공합니다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Getter
@Builder
@AllArgsConstructor
public class NoticeResponse {

    @Schema(description = "공지 ID")
    private Long id;

    public static NoticeResponse of(Notice notice) {
        return NoticeResponse.builder().id(notice.getId()).build();
    }
}