package com.bukadong.tcg.api.notice.dto.response;

import com.bukadong.tcg.api.notice.entity.Notice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 상세 응답 DTO
 * <P>
 * 제목, 본문, 작성자(닉네임), 조회수, 생성/수정일, 첨부파일(이미지/일반)을 담습니다.
 * </P>
 * 
 * @PARAM id 공지 ID
 * @PARAM title 제목
 * @PARAM text 본문
 * @PARAM nickname 작성자 닉네임
 * @PARAM viewCount 조회수
 * @PARAM imageUrls 이미지 URL 목록
 * @PARAM attachmentUrls 일반 첨부 URL 목록(zip/pdf 등)
 * @PARAM createdAt 생성일시
 * @PARAM updatedAt 수정일시
 * @RETURN 없음
 */
public record NoticeDetailDto(Long id, String title, String text, String nickname, Long viewCount,
        List<String> imageUrls, List<String> attachmentUrls, LocalDateTime createdAt, LocalDateTime updatedAt) {
    /**
     * Notice 엔티티로 상세 DTO를 생성한다.
     * 
     * @PARAM n Notice 엔티티
     * @PARAM imageUrls 이미지 URL
     * @PARAM attachmentUrls 일반 첨부 URL
     * @RETURN NoticeDetailDto
     */
    public static NoticeDetailDto of(Notice n, List<String> imageUrls, List<String> attachmentUrls) {
        return new NoticeDetailDto(n.getId(), n.getTitle(), n.getText(), n.getAuthor().getNickname(), n.getViewCount(),
                imageUrls, attachmentUrls, n.getCreatedAt(), n.getUpdatedAt());
    }
}
