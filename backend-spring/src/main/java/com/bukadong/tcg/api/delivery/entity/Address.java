package com.bukadong.tcg.api.delivery.entity;

import com.bukadong.tcg.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 회원 배송지
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_address_member_place : (member_id, place_name) 복합 고유
 * 제약 생성
 * - @Index idx_address_member : member_id 인덱스 생성
 * - @Index idx_address_zipcode : zipcode 인덱스 생성
 */
@Entity
@Table(name = "address", uniqueConstraints = {
        @UniqueConstraint(name = "uk_address_member_place", columnNames = { "member_id", "place_name" })
}, indexes = {
        @Index(name = "idx_address_member", columnList = "member_id"),
        @Index(name = "idx_address_zipcode", columnList = "zipcode")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    /** ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소유 회원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 장소 별칭(예: 집, 회사) */
    @Column(name = "place_name", length = 30)
    private String placeName;

    /** 수령인 이름 */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /** 연락처 */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /** 기본 주소 */
    @Column(name = "base_address", nullable = false, length = 200)
    private String baseAddress;

    /** 상세 주소 */
    @Column(name = "address_detail", nullable = false, length = 150)
    private String addressDetail;

    /** 우편번호 */
    @Column(name = "zipcode", nullable = false, length = 10)
    private String zipcode;
}
