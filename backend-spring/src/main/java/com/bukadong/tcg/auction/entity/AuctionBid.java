package com.bukadong.tcg.auction.entity;

import com.bukadong.tcg.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 경매 입찰
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @Index idx_bid_auction : auction_id 컬럼에 대한 보조 인덱스 생성
 * - @Index idx_bid_member : member_id 컬럼에 대한 보조 인덱스 생성
 */
@Entity
@Table(name = "auction_bid", indexes = {
        @Index(name = "idx_bid_auction", columnList = "auction_id"),
        @Index(name = "idx_bid_member", columnList = "member_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionBid {

    /** ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 경매 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    /** 입찰자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 입찰가 (코인 소수 8자리) */
    @Column(name = "bid_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal bidPrice;

    /** 입찰 방식 */
    @Enumerated(EnumType.STRING)
    @Column(name = "bid_type", nullable = false, length = 10)
    private AuctionBidType bidType;

    /** 입찰 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuctionBidStatus status;

    /** 체인 Tx Hash (선택) */
    @Column(name = "tx_hash", length = 100)
    private String txHash;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
