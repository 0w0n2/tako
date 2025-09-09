package com.bukadong.tcg.api.media.dto;

import com.bukadong.tcg.api.media.entity.Media;
import com.bukadong.tcg.api.media.entity.MediaKind;

/**
 * 미디어 조회용 DTO.
 * <p>
 * {@link Media} 엔티티에서 필요한 최소한의 정보만 추출하여 클라이언트 응답에 사용한다.
 * </p>
 * <ul>
 * <li>{@code id} - 미디어 ID (PK)</li>
 * <li>{@code url} - 미디어 파일 접근 URL</li>
 * <li>{@code mediaKind} - 미디어 종류 (IMAGE, VIDEO)</li>
 * <li>{@code mimeType} - MIME 타입 (예: image/jpeg, video/mp4)</li>
 * <li>{@code seqNo} - 순번 (1 = 대표 이미지, 그 외는 추가 이미지/영상)</li>
 * </ul>
 */
public record MediaDto(Long id, String url, MediaKind mediaKind, String mimeType, Integer seqNo) {

    /**
     * {@link Media} 엔티티를 {@link MediaDto}로 변환한다.
     *
     * @param m 변환할 Media 엔티티
     * @return 변환된 MediaDto
     */
    public static MediaDto of(Media m) {
        return new MediaDto(m.getId(), m.getUrl(), m.getMediaKind(), m.getMimeType(), m.getSeqNo());
    }
}
