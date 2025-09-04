package com.bukadong.tcg.member.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 회원 소셜 계정 연동 엔티티.
 * TCG.sql의 `회원_소셜` 테이블 매핑.
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_msocial_email : email 고유 제약 생성
 * - @UniqueConstraint uk_msocial_provider : (provider_name, provider_id) 복합 고유
 * 제약 생성
 * - @Index idx_msocial_member : member_id 인덱스 생성
 * - @Index idx_msocial_pname : provider_name 인덱스 생성
 */
@Entity
@Table(name = "member_social", uniqueConstraints = {
        @UniqueConstraint(name = "uk_msocial_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_msocial_provider", columnNames = { "provider_name", "provider_id" })
}, indexes = {
        @Index(name = "idx_msocial_member", columnList = "member_id"),
        @Index(name = "idx_msocial_pname", columnList = "provider_name")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSocial {

    /** PK */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 연동 대상 회원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 소셜 이메일 — 고유 제약은 @Table.uniqueConstraints로 관리 */
    @Column(name = "email", nullable = false, length = 50)
    private String email;

    /** 제공자 (GOOGLE/NAVER/KAKAO) */
    @Enumerated(EnumType.STRING)
    @Column(name = "provider_name", nullable = false, length = 20)
    private SocialProvider providerName;

    /** 제공자 내부 식별자 — (provider_name, provider_id) 복합 고유 */
    @Column(name = "provider_id", nullable = false, length = 255)
    private String provider;
}
