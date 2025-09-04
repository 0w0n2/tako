package com.bukadong.tcg.notification.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 알림종류 테이블
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_noti_type_type : type 컬럼 고유 제약 생성
 */
@Entity
@Table(name = "notification_type", uniqueConstraints = {
        @UniqueConstraint(name = "uk_noti_type_type", columnNames = "type")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationType {

    /** ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 종류 enum */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type_kind", nullable = false, length = 20)
    private NotificationTypeKind type;
}
