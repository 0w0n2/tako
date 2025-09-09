package com.bukadong.tcg.api.auction.entity;

import com.bukadong.tcg.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 경매 입찰 엔티티
 *
 * 매핑 정보:
 * - Table: 입찰
 * - @Index: auction_id, member_id에 각각 인덱스 생성
 * - FK: auction_id → 경매.id, member_id → 회원.id
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
    @JoinColumn(name = "auction_id", nullable = false, foreignKey = @ForeignKey(name = "FK_auction_bid_auction"))
    private Auction auction;

    /** 입찰자 (회원) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "FK_auction_bid_member"))
    private Member member;

    /** 입찰가 (코인 소수 8자리) */
    @Column(name = "bid_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal bidPrice;

    /** 입찰 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private AuctionBidStatus status;

    /** 체인 Tx Hash (있다면) */
    @Column(name = "tx_hash", length = 255)
    private String txHash;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 저장 전 생성일 자동 세팅 */
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
