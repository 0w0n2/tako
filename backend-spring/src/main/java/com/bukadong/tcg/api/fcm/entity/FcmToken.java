package com.bukadong.tcg.api.fcm.entity;

import com.bukadong.tcg.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * FCM 단말 토큰 엔티티.
 * <p>
 * 하나의 회원이 여러 기기(브라우저/앱)를 사용할 수 있으므로 (member, token) 고유 제약이 아닌 token 자체를 UNIQUE
 * 로 둔다. 만료되거나 로그아웃 시 제거.
 * </p>
 */
@Entity
@Table(name = "fcm_token", uniqueConstraints = {
        @UniqueConstraint(name = "uk_fcm_token_token", columnNames = "token") }, indexes = {
                @Index(name = "idx_fcm_token_member", columnList = "member_id") })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fcm_token_member"))
    private Member member;

    @Column(name = "token", nullable = false, length = 512)
    private String token;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
