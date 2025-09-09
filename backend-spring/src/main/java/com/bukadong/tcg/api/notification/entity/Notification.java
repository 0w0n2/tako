package com.bukadong.tcg.api.notification.entity;

import java.time.LocalDateTime;

import com.bukadong.tcg.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 알림 엔티티
 *
 * <p>
 * 회원이 수신한 알림 내역을 저장한다.
 * </p>
 *
 * <ul>
 * <li>id는 단일 PK로 사용</li>
 * <li>member_id, notification_type_id는 FK 매핑</li>
 * <li>member_id, is_read, created_at, notification_type_id에 인덱스 생성</li>
 * </ul>
 */
@Entity
@Table(name = "notification", indexes = {
        @Index(name = "idx_noti_member", columnList = "member_id"),
        @Index(name = "idx_noti_member_read", columnList = "member_id,is_read"),
        @Index(name = "idx_noti_member_created", columnList = "member_id,created_at"),
        @Index(name = "idx_noti_type", columnList = "notification_type_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    /** 알림 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 수신자 (회원) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "FK_notification_member"))
    private Member member;

    /** 알림 종류 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_type_id", nullable = false, foreignKey = @ForeignKey(name = "FK_notification_type"))
    private NotificationType notificationType;

    /** 알림 내용 */
    @Column(name = "content", nullable = false, length = 100)
    private String content;

    /** 읽음 여부 */
    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 저장 전 생성일 자동 세팅 */
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
