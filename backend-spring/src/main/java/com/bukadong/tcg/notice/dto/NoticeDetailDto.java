package com.bukadong.tcg.notice.dto;

import com.bukadong.tcg.notice.entity.Notice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 상세 응답 DTO.
 *
 * <p>
 * 제목, 본문, 작성자, 조회수, 생성/수정일, 첨부파일을 담는다.
 * </p>
 */
public record NoticeDetailDto(
        Long id,
        String title,
        String text,
        Long authorId,
        String authorNickname,
        Long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<NoticeAttachmentDto> attachments) {
    /**
     * Notice 엔티티와 첨부 DTO 리스트로 상세 DTO를 생성한다.
     *
     * @param n     Notice 엔티티
     * @param files 첨부 DTO 리스트
     * @return NoticeDetailDto
     */
    public static NoticeDetailDto of(Notice n, List<NoticeAttachmentDto> files) {
        return new NoticeDetailDto(
                n.getId(),
                n.getTitle(),
                n.getText(),
                n.getAuthor().getId(),
                n.getAuthor().getNickname(),
                n.getViewCount(),
                n.getCreatedAt(),
                n.getUpdatedAt(),
                files);
    }
}
