package com.bukadong.tcg.auction.entity;

import jakarta.persistence.*;
import lombok.*;

import com.bukadong.tcg.common.base.BaseEntity;

/**
 * 경매 결과(정산/체결 정보)
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - uk_auction_result_auction : auction_id UNIQUE 제약 생성
 * - uk_auction_result_settle_tx_hash : settle_tx_hash UNIQUE 제약 생성
 * - idx_aucres_bid : auction_bid_id 보조 인덱스 생성
 */
@Entity
@Table(name = "auction_result", uniqueConstraints = {
        @UniqueConstraint(name = "uk_auction_result_auction", columnNames = "auction_id"),
        @UniqueConstraint(name = "uk_auction_result_settle_tx_hash", columnNames = "settle_tx_hash")
}, indexes = {
        @Index(name = "idx_aucres_bid", columnList = "auction_bid_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionResult extends BaseEntity {

    /** ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 경매 (유일) — @Table의 uk_auction_result_auction으로 1:1 보장 */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    /** 낙찰 입찰 ID — Bid 엔티티와의 직접 연관이 필요하면 ManyToOne으로 바꿀 수 있음 */
    @Column(name = "auction_bid_id", nullable = false)
    private Long auctionBidId;

    /** 정산 여부 */
    @Column(name = "settled_flag", nullable = false)
    private boolean settledFlag;

    /** 정산 트랜잭션 해시 — @Table의 uk_auction_result_settle_tx_hash로 유니크 보장 */
    @Column(name = "settle_tx_hash", length = 100)
    private String settleTxHash;
}
