package com.bukadong.tcg.api.delivery.entity;

import com.bukadong.tcg.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 기본 배송지: 회원당 최대 1개 매핑
 */
@Entity
@Table(name = "default_address", uniqueConstraints = {
        @UniqueConstraint(name = "uk_default_address_member", columnNames = { "member_id" }) }, indexes = {
                @Index(name = "idx_default_address_member", columnList = "member_id"),
                @Index(name = "idx_default_address_address", columnList = "address_id") })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefaultAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;
}
