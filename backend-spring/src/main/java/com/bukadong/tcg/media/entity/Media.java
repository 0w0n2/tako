package com.bukadong.tcg.media.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 미디어 파일 메타정보
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_media_owner_seq : (type, owner_id, seq_no) 복합 고유 제약 생성
 * - @UniqueConstraint uk_media_url : url 컬럼 고유 제약 생성
 * - @Index idx_media_type_owner: (type, owner_id) 복합 인덱스 생성
 * - @Index idx_media_owner : owner_id 인덱스 생성
 */
@Entity
@Table(name = "media", uniqueConstraints = {
        @UniqueConstraint(name = "uk_media_owner_seq", columnNames = { "type", "owner_id", "seq_no" }),
        @UniqueConstraint(name = "uk_media_url", columnNames = "url")
}, indexes = {
        @Index(name = "idx_media_type_owner", columnList = "type,owner_id"),
        @Index(name = "idx_media_owner", columnList = "owner_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

    /** ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** AUCTION/REVIEW */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaType type;

    /** 소유자 ID (경매ID or 후기ID) */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /** 접근 URL — 고유 제약은 @Table.uniqueConstraints로 관리 */
    @Column(nullable = false, length = 200)
    private String url;

    /** IMAGE/VIDEO */
    @Enumerated(EnumType.STRING)
    @Column(name = "media_kind", nullable = false, length = 10)
    private MediaKind mediaKind;

    /** MIME 타입 */
    @Column(name = "mime_type", length = 30)
    private String mimeType;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 대표 순번: 1=대표 */
    @Column(name = "seq_no", nullable = false)
    private Integer seqNo;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
