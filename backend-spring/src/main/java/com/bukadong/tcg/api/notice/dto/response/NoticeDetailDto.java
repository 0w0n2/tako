package com.bukadong.tcg.api.notice.dto.response;

import com.bukadong.tcg.api.media.dto.MediaDto;
import com.bukadong.tcg.api.notice.entity.Notice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 상세 응답 DTO
 * <p>
 * 제목, 본문, 작성자(닉네임), 조회수, 생성/수정일, 첨부파일을 담는다.
 * </p>
 *
 * @param id          공지 ID
 * @param title       제목
 * @param text        본문
 * @param nickname    작성자 닉네임
 * @param viewCount   조회수
 * @param createdAt   생성일시
 * @param updatedAt   수정일시
 * @param attachments 첨부파일 목록
 * @return 없음
 */
public record NoticeDetailDto(Long id, String title, String text, String nickname, Long viewCount,
        List<String> imageUrls, LocalDateTime createdAt, LocalDateTime updatedAt) {
    /**
     * Notice 엔티티로 상세 DTO를 생성한다.
     *
     * @param n Notice 엔티티
     * @return NoticeDetailDto
     */
    public static NoticeDetailDto of(Notice n, List<String> imageUrls) {
        return new NoticeDetailDto(n.getId(), n.getTitle(), n.getText(), n.getAuthor().getNickname(), n.getViewCount(),
                imageUrls, n.getCreatedAt(), n.getUpdatedAt());
    }
}
