package com.bukadong.tcg.notice.dto.response;

import com.bukadong.tcg.notice.entity.Notice;

import java.time.LocalDateTime;

/**
 * 공지사항 목록 응답용 요약 DTO.
 * <p>
 * 제목, 작성자, 조회수, 생성일을 포함한다.
 * </p>
 */
public record NoticeSummaryDto(Long id, String title, String nickname, Long viewCount,
        LocalDateTime createdAt) {
    /**
     * Notice 엔티티를 요약 DTO로 변환한다.
     *
     * @param n Notice 엔티티
     * @return NoticeSummaryDto
     */
    public static NoticeSummaryDto from(Notice n) {
        return new NoticeSummaryDto(n.getId(), n.getTitle(), n.getAuthor().getNickname(),
                n.getViewCount(), n.getCreatedAt());
    }
}
