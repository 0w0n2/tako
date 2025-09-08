package com.bukadong.tcg.push.entity;

import com.bukadong.tcg.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 웹푸쉬(FCM/Web Push) 구독
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_push_endpoint : endpoint 고유 제약 생성
 * - @Index idx_push_member : member_id 인덱스 생성
 */
@Entity
@Table(name = "webpush_subscription", uniqueConstraints = {
        @UniqueConstraint(name = "uk_push_endpoint", columnNames = "endpoint")
}, indexes = {
        @Index(name = "idx_push_member", columnList = "member_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebPushSubscription {

    /** ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 구독자 회원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** Push endpoint URL — 고유 제약은 @Table.uniqueConstraints로 관리 */
    @Column(name = "endpoint", nullable = false, length = 1024)
    private String endpoint;

    /** VAPID 공개키 */
    @Column(name = "p256dh", nullable = false, length = 255)
    private String p256dh;

    /** 인증 토큰 */
    @Column(name = "auth", nullable = false, length = 255)
    private String auth;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
