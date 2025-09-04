package com.bukadong.tcg.member.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

/**
 * 회원 정보를 저장하는 엔티티.
 * TCG.sql의 `회원` 테이블을 매핑한다.
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_member_uuid : uuid 고유 제약 생성
 * - @UniqueConstraint uk_member_email : email 고유 제약 생성
 * - @UniqueConstraint uk_member_nickname : nickname 고유 제약 생성
 * - @Index idx_member_role : role 인덱스 생성
 * - @Index idx_member_deleted : is_deleted 인덱스 생성
 */
@Entity
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_uuid", columnNames = "uuid"),
        @UniqueConstraint(name = "uk_member_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_member_nickname", columnNames = "nickname")
}, indexes = {
        @Index(name = "idx_member_role", columnList = "role"),
        @Index(name = "idx_member_deleted", columnList = "is_deleted")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    /** PK (BIGINT, AUTO INCREMENT 가정) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 고유 UUID */
    @Column(nullable = false, length = 60)
    private String uuid;

    /** 이메일 (로그인 ID) */
    @Column(nullable = false, length = 100)
    private String email;

    /** 암호화된 비밀번호 */
    @Column(nullable = false, length = 200)
    private String password;

    /** 닉네임 */
    @Column(nullable = false, length = 30)
    private String nickname;

    /** 탈퇴 여부: false=활성, true=탈퇴 */
    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /** 탈퇴 일시 */
    private LocalDateTime deletedAt;

    /** 권한 (USER / ADMIN) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    /** 생성일 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정일 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** JPA 라이프사이클 훅으로 createdAt/updatedAt 자동 세팅 */
    @PrePersist
    public void onCreate() {
        final LocalDateTime now = LocalDateTime.now();
        this.createdAt = (this.createdAt == null) ? now : this.createdAt;
        this.updatedAt = (this.updatedAt == null) ? now : this.updatedAt;
        if (this.isDeleted == null)
            this.isDeleted = false;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** 소프트 삭제(회원 탈퇴 처리) */
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
