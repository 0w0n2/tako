package com.bukadong.tcg.notice.dto.response;

import com.bukadong.tcg.media.entity.Media;
import com.bukadong.tcg.media.entity.MediaKind;

import java.time.LocalDateTime;

/**
 * 공지사항 첨부파일 응답 DTO.
 * <p>
 * Media 테이블 기반으로 파일 표시 정보를 제공한다.
 * </p>
 */
public record NoticeAttachmentDto(Long id, String url, MediaKind mediaKind, String mimeType,
        Integer seqNo) {
    /**
     * Media 엔티티를 첨부 DTO로 변환한다.
     *
     * @param m Media 엔티티
     * @return NoticeAttachmentDto
     */
    public static NoticeAttachmentDto from(Media m) {
        return new NoticeAttachmentDto(m.getId(), m.getUrl(), m.getMediaKind(), m.getMimeType(),
                m.getSeqNo());
    }
}
