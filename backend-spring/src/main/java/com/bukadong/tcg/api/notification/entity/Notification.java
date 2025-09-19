package com.bukadong.tcg.api.notification.entity;

import java.time.LocalDateTime;

import com.bukadong.tcg.global.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * 알림 엔티티
 * <P>
 * 유저에게 전달되는 단일 알림. 원인 리소스(경매/카드/문의 등)의 식별자는 causeId에 저장한다. targetUrl은 빈 문자열을
 * 기본값으로 유지한다(추후 주입).
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Entity
@Table(name = "notification", indexes = { @Index(name = "idx_notification_member", columnList = "member_id"),
        @Index(name = "idx_notification_created", columnList = "created_at") })
@Getter
@NoArgsConstructor
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 수신자 */
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /** 알림 타입 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notification_type"))
    private NotificationType type;

    /** 원인 리소스 ID(대개 경매ID/카드ID/문의ID) */
    @Column(name = "cause_id")
    private Long causeId;

    /** 제목 */
    @Column(name = "title", length = 100, nullable = false)
    private String title;

    /** 본문 */
    @Lob
    @Column(name = "message", nullable = false)
    private String message;

    /** 클릭 타겟 URL(지금은 빈 문자열로 저장) */
    @Column(name = "target_url", length = 255, nullable = false)
    private String targetUrl;

    /** 읽음 여부 */
    @Column(name = "is_read", nullable = false)
    private boolean read;

    /** 읽은 시각(읽지 않은 경우 null) */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Builder
    private Notification(Long memberId, NotificationType type, Long causeId, String title, String message,
            String targetUrl) {
        this.memberId = memberId;
        this.type = type;
        this.causeId = causeId;
        this.title = title;
        this.message = message;
        this.targetUrl = (targetUrl != null) ? targetUrl : ""; // 지금은 빈 문자열 유지
        this.read = false;
        this.readAt = null;
    }

    /**
     * 읽음 처리
     * <P>
     * KST 기준 시간은 서버 표준 타임존 설정(Asia/Seoul)에 따르며, 여기서는 단순히 now()를 사용한다.
     * </P>
     * 
     * @PARAM when 읽은 시각(없으면 now)
     * @RETURN 없음
     */
    public void markRead(LocalDateTime when) {
        this.read = true;
        this.readAt = (when != null) ? when : LocalDateTime.now();
    }
}
