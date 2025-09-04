package com.bukadong.tcg.notification.entity;

import com.bukadong.tcg.common.base.BaseEntity;
import com.bukadong.tcg.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 알림 내역 (받은 알림)
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @Index idx_noti_member : member_id 인덱스 생성
 * - @Index idx_noti_member_read : (member_id, is_read) 복합 인덱스 생성
 * - @Index idx_noti_member_created : (member_id, created_at) 복합 인덱스 생성
 * - @Index idx_noti_type : notification_type_id 인덱스 생성
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
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noti_id")
    private Long id;

    /** 수신자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member receiver;

    /** 알림 종류 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_type_id", nullable = false)
    private NotificationType notificationType;

    /** 알림 내용 */
    @Column(nullable = false, length = 100)
    private String content;

    /** 읽음 여부 */
    @Column(name = "is_read", nullable = false)
    private boolean read;
}
