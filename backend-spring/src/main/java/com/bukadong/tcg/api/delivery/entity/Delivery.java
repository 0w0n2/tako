package com.bukadong.tcg.api.delivery.entity;

import jakarta.persistence.*;
import lombok.*;

import com.bukadong.tcg.global.common.base.BaseEntity;

/**
 * 배송
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_delivery_tracking_no : tracking_number 고유 제약 생성
 * - @Index idx_delivery_sender : sender_address_id 인덱스 생성
 * - @Index idx_delivery_recipient : recipient_address_id 인덱스 생성
 * - @Index idx_delivery_status : status 인덱스 생성
 */
@Entity
@Table(name = "delivery", uniqueConstraints = {
        @UniqueConstraint(name = "uk_delivery_tracking_no", columnNames = "tracking_number")
}, indexes = {
        @Index(name = "idx_delivery_sender", columnList = "sender_address_id"),
        @Index(name = "idx_delivery_recipient", columnList = "recipient_address_id"),
        @Index(name = "idx_delivery_status", columnList = "status")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 보내는 주소지 (필수) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_address_id", nullable = false)
    private Address senderAddress;

    /** 받는 주소지 (필수) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_address_id", nullable = false)
    private Address recipientAddress;

    /** 운송장 번호 (선택) — 고유 제약은 @Table.uniqueConstraints로 관리 */
    // 운송장 번호는 택배사에서 발급받는 값으로, 배송이 시작되기 전에는 없을 수 있음
    @Column(name = "tracking_number", length = 50)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeliveryStatus status;
}
