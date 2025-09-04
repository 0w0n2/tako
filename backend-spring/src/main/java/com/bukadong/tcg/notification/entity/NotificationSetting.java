package com.bukadong.tcg.notification.entity;

import com.bukadong.tcg.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 알림 설정 (회원별 알림종류 on/off)
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_noti_setting_member_type : (member_id,
 * notification_id) 복합 고유 제약 생성
 * - @Index idx_noti_setting_member : member_id 인덱스 생성
 * - @Index idx_noti_setting_type : notification_id 인덱스 생성
 */
@Entity
@Table(name = "notification_setting", uniqueConstraints = {
        @UniqueConstraint(name = "uk_noti_setting_member_type", columnNames = { "member_id", "notification_id" })
}, indexes = {
        @Index(name = "idx_noti_setting_member", columnList = "member_id"),
        @Index(name = "idx_noti_setting_type", columnList = "notification_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSetting {

    /** ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 회원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 알림 종류 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    private NotificationType notificationType;

    /** on/off (NULL = 미설정) */
    @Column(name = "Field")
    private Boolean enabled;
}
