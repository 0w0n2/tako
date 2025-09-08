package com.bukadong.tcg.member.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

/**
 * 회원 엔티티
 *
 * <p>
 * - uuid, email, nickname은 UNIQUE 제약
 * - role, is_deleted에 인덱스 생성
 * </p>
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

    /** PK */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 고유 UUID */
    @Column(nullable = false, length = 60)
    private String uuid;

    /** 이메일 (로그인 ID) */
    @Column(nullable = false, length = 100)
    private String email;

    /** 암호화된 비밀번호 */
    @Column(nullable = false, length = 255)
    private String password;

    /** 닉네임 (2~6자) */
    @Column(nullable = false, length = 6)
    private String nickname;

    /** 소개글 */
    @Column(nullable = false, length = 255)
    private String introduction;

    /** 탈퇴 여부 */
    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /** 탈퇴 시각 */
    private LocalDateTime deletedAt;

    /** 권한 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    /** 생성일 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정일 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 엔티티 생성 전 */
    @PrePersist
    public void onCreate() {
        final LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.isDeleted == null)
            this.isDeleted = false;
    }

    /** 엔티티 수정 전 */
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** 소프트 삭제 */
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
