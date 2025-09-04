package com.bukadong.tcg.auction.entity;

import com.bukadong.tcg.card.entity.PhysicalCard;
import com.bukadong.tcg.common.base.BaseEntity;
import com.bukadong.tcg.delivery.entity.Delivery;
import com.bukadong.tcg.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 경매
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_auction_code : code 컬럼 고유 제약 생성
 * - @Index idx_auc_owner : member_id 인덱스 생성
 * - @Index idx_auc_delivery : delivery_id 인덱스 생성
 * - @Index idx_auc_pcard : physical_card_id 인덱스 생성
 * - @Index idx_auc_start_end : (start_datetime, end_datetime) 복합 인덱스 생성
 */
@Entity
@Table(name = "auction", uniqueConstraints = {
        @UniqueConstraint(name = "uk_auction_code", columnNames = { "code" })
}, indexes = {
        @Index(name = "idx_auc_owner", columnList = "member_id"),
        @Index(name = "idx_auc_delivery", columnList = "delivery_id"),
        @Index(name = "idx_auc_pcard", columnList = "physical_card_id"),
        @Index(name = "idx_auc_start_end", columnList = "start_datetime,end_datetime")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction extends BaseEntity {
    /** ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 경매 생성자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member owner;

    /** 배송(선택) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    /** 실물 카드 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "physical_card_id", nullable = false)
    private PhysicalCard physicalCard;

    /** 카드 컨디션 등급 (예: PS/NM 등 2자리) */
    @Column(name = "grade", nullable = false, length = 2)
    private String grade;

    /** 경매 코드 (고유) — URL/공유 식별 등 외부 노출에 사용 */
    @Column(nullable = false, length = 40)
    private String code;

    /** 경매 제목 */
    @Column(nullable = false, length = 40)
    private String title;

    /** 경매 상세 설명 */
    @Lob
    @Column(name = "detail", nullable = false)
    private String detail;

    /** 시작 가격 (코인 단위, 소수점 8자리) */
    @Column(name = "start_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal startPrice;

    /** 현재가 (코인 단위, 소수점 8자리) */
    @Column(name = "current_price", precision = 20, scale = 8, nullable = false)
    private BigDecimal currentPrice;

    /** 입찰 단위 */
    @Convert(converter = com.bukadong.tcg.auction.converter.AuctionBidUnitConverter.class)
    @Column(name = "bid_unit", nullable = false, length = 10)
    private AuctionBidUnit bidUnit;

    /** 시작 일시 */
    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    /** 종료 일시 */
    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;

    /** 경매 기간 (일) */
    @Builder.Default
    @Column(name = "duration_days", nullable = false)
    private Integer durationDays = 1;

    /** 즉시 구매 가능 여부 */
    @Column(name = "buy_now_flag", nullable = false)
    private boolean buyNowFlag;

    /** 즉시 구매 가격 */
    @Column(name = "buy_now_price", precision = 20, scale = 8)
    private BigDecimal buyNowPrice;

    /** 자동 재입찰 가능 여부 */
    @Column(name = "auto_rebid_flag", nullable = false)
    private boolean autoRebidFlag;

    /** 자동 재입찰 가격 */
    @Column(name = "auto_rebid_price", precision = 20, scale = 8)
    private BigDecimal autoRebidPrice;

    /** 연장 가능 여부 */
    @Builder.Default
    @Column(name = "extension_flag", nullable = false)
    private boolean extensionFlag = true;

    /** 취소 가능 여부 */
    @Column(name = "canceled_flag", nullable = false)
    private boolean canceledFlag;

    /** 세금 포함 여부 */
    @Column(name = "tax_flag", nullable = false)
    private boolean taxFlag;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 수정 일시 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 엔티티 저장 전, currentPrice를 startPrice로 초기화 */
    @PrePersist
    void prePersistAuction() {
        // currentPrice가 null이면 startPrice로 초기화
        if (currentPrice == null && startPrice != null) {
            currentPrice = startPrice;
        }
    }
}
