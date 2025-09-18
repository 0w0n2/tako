package com.bukadong.tcg.api.notification.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 알림 종류 엔티티
 * <p>
 * 회원이 받을 수 있는 알림의 종류를 정의한다.
 * </p>
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
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private NotificationTypeCode code;
    /** 표시명(선택) */
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    /** 설명(선택) */
    @Column(name = "description", length = 255)
    private String description;

    private NotificationType(NotificationTypeCode code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 팩토리
     * <P>
     * code/name/description으로 생성한다.
     * </P>
     * 
     * @PARAM code 코드
     * @PARAM name 표시명
     * @PARAM description 설명
     * @RETURN NotificationType
     */
    public static NotificationType of(NotificationTypeCode code, String name, String description) {
        return new NotificationType(code, name, description);
    }
}
