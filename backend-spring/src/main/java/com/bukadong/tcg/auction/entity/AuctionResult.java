package com.bukadong.tcg.auction.entity;

import com.bukadong.tcg.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 경매 결과 엔티티
 *
 * <p>
 * 경매 종료 후 정산/체결 정보를 저장한다.
 * </p>
 *
 * <ul>
 * <li>경매 ID(auction_id)는 하나의 경매에 하나의 결과만 존재하도록 Unique 제약</li>
 * <li>낙찰 입찰 ID(auction_bid_id)는 입찰 테이블의 PK와 연결</li>
 * <li>정산 트랜잭션 해시(settle_tx_hash)는 블록체인 트랜잭션을 식별하며 Unique 보장</li>
 * <li>정산 여부(settled_flag)는 미정산(0), 정산완료(1)을 표시</li>
 * </ul>
 */
@Entity
@Table(name = "auction_result", uniqueConstraints = {
                @UniqueConstraint(name = "uk_auction_result_auction", columnNames = "auction_id"),
                @UniqueConstraint(name = "uk_auction_result_settle_tx_hash", columnNames = "settle_tx_hash")
}, indexes = {
                @Index(name = "idx_aucres_auction", columnList = "auction_id"),
                @Index(name = "idx_aucres_bid", columnList = "auction_bid_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionResult extends BaseEntity {

        /** 경매 결과 ID (PK) */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /** 경매 (1:1 매핑, 하나의 경매는 하나의 결과만 가진다) */
        @OneToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "auction_id", nullable = false, foreignKey = @ForeignKey(name = "FK_auction_result_auction"))
        private Auction auction;

        /** 낙찰 입찰 (1:1 매핑, 하나의 결과는 하나의 낙찰 입찰과 연결된다) */
        @OneToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "auction_bid_id", nullable = false, foreignKey = @ForeignKey(name = "FK_auction_result_bid"))
        private AuctionBid auctionBid;

        /** 정산 여부 (false=미정산, true=정산완료) */
        @Column(name = "settled_flag", nullable = false)
        private boolean settledFlag;

        /** 정산 트랜잭션 해시 (블록체인 트랜잭션 식별, Unique) */
        @Column(name = "settle_tx_hash", length = 100, unique = true)
        private String settleTxHash;
}
