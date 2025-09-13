package com.bukadong.tcg.api.media.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 미디어 파일 메타정보 엔티티
 * <p>
 * 경매/후기/카드/카테고리/회원 등의 리소스에 연결되는 이미지·영상의 메타데이터를 저장한다.
 * </p>
 * <ul>
 * <li>(type, owner_id, seq_no) 복합 고유 제약으로 리소스별 대표/순번 유일성 보장</li>
 * <li>url은 고유(UNIQUE)하게 관리</li>
 * <li>(type, owner_id) 조회용 복합 인덱스와 owner_id 단일 인덱스 제공</li>
 * </ul>
 */
@Entity
@Table(name = "media", uniqueConstraints = {
        @UniqueConstraint(name = "uk_media_owner_seq", columnNames = { "type", "owner_id", "seq_no" }),
        @UniqueConstraint(name = "uk_media_s3key", columnNames = "s3key") }, indexes = {
                @Index(name = "idx_media_type_owner", columnList = "type,owner_id"),
                @Index(name = "idx_media_owner", columnList = "owner_id") })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

    /** 미디어 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소유 리소스 타입 (예: AUCTION_ITEM, MEMBER_PROFILE 등) */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private MediaType type;

    /** 소유 리소스 ID (경매ID/후기ID/카드ID/카테고리ID/회원ID 등) */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /** 접근 key (고유) */
    @Column(name = "s3key", nullable = false, length = 255, unique = true)
    private String s3key;

    /** 미디어 종류 (IMAGE/VIDEO) */
    @Enumerated(EnumType.STRING)
    @Column(name = "media_kind", nullable = false, length = 10)
    private MediaKind mediaKind;

    /** MIME 타입 (예: image/jpeg) */
    @Column(name = "mime_type", length = 30)
    private String mimeType;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 리소스 내 정렬 순번 (1=대표) */
    @Column(name = "seq_no", nullable = false)
    private Integer seqNo;

    /** 저장 전 생성일시 자동 설정 */
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * 순번 설정
     * 
     * @param seq
     */
    public void setSeqNo(int seq) {
        this.seqNo = seq;
    }
}
