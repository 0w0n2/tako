package com.bukadong.tcg.api.notification.entity;

import com.bukadong.tcg.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 알림 설정 엔티티
 *
 * <p>
 * 회원별 알림 종류에 대해 활성화 여부(on/off)를 저장한다.
 * </p>
 *
 * <ul>
 * <li>(member_id, notification_id) 복합 고유 제약</li>
 * <li>member_id, notification_id 각각 인덱스 생성</li>
 * <li>enabled 기본값 = false</li>
 * </ul>
 */
@Entity
@Table(name = "notification_setting", uniqueConstraints = {
                @UniqueConstraint(name = "uk_noti_setting_member_type", columnNames = { "member_id",
                                "notification_id" })
}, indexes = {
                @Index(name = "idx_noti_setting_member", columnList = "member_id"),
                @Index(name = "idx_noti_setting_type", columnList = "notification_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSetting {

        /** 알림 설정 ID (PK) */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /** 회원 */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "FK_notification_setting_member"))
        private Member member;

        /** 알림 종류 */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "notification_type_id", nullable = false, foreignKey = @ForeignKey(name = "FK_notification_setting_type"))
        private NotificationType notificationType;

        /** on/off (기본값 false) */
        @Builder.Default
        @Column(name = "enabled", nullable = false)
        private Boolean enabled = false;
}
