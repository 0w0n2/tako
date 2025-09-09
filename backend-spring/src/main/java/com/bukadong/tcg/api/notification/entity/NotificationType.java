package com.bukadong.tcg.api.notification.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 알림 종류 엔티티
 *
 * <p>
 * 회원이 받을 수 있는 알림의 종류를 정의한다.
 * </p>
 *
 * <ul>
 * <li>id는 PK</li>
 * <li>type은 ENUM (WISH_AUCTION, WISH_CARD, KEYWORD)</li>
 * </ul>
 */
@Entity
@Table(name = "notification_type")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationType {

    /** 알림 종류 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 알림 종류 */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationTypeKind type;
}
