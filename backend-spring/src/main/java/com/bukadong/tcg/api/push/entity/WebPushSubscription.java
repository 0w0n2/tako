package com.bukadong.tcg.api.push.entity;

import com.bukadong.tcg.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 웹푸쉬(FCM/Web Push) 구독 엔티티
 * <p>
 * 회원이 등록한 WebPush/FCM 구독 정보를 저장한다.
 * </p>
 * <ul>
 * <li>id는 BIGINT AUTO_INCREMENT</li>
 * <li>endpoint는 고유(UNIQUE)</li>
 * <li>member_id에 인덱스 존재</li>
 * </ul>
 */
@Entity
@Table(name = "webpush_subscription", uniqueConstraints = {
        @UniqueConstraint(name = "uk_push_endpoint", columnNames = "endpoint") }, indexes = {
                @Index(name = "idx_push_member", columnList = "member_id") })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebPushSubscription {

    /** 구독 ID (PK, BIGINT AUTO_INCREMENT) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 구독자 회원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "FK_webpush_subscription_member"))
    private Member member;

    /** Push endpoint URL (고유) */
    @Column(name = "endpoint", nullable = false, length = 1024, unique = true)
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

    /** 저장 전 createdAt 자동 설정 */
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneOffset.UTC);
        }
    }
}
