package com.bukadong.tcg.trade.entity;

import java.time.LocalDateTime;

import com.bukadong.tcg.auction.entity.Auction;
import com.bukadong.tcg.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 거래내역
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_trade_auction_type : (auction_id, type) 복합 고유 제약 생성
 * - @Index idx_trade_member : member_id 인덱스 생성
 * - @Index idx_trade_auction : auction_id 인덱스 생성
 */
@Entity
@Table(name = "trade_history", uniqueConstraints = {
                @UniqueConstraint(name = "uk_trade_auction_type", columnNames = { "auction_id", "type" })
}, indexes = {
                @Index(name = "idx_trade_member", columnList = "member_id"),
                @Index(name = "idx_trade_auction", columnList = "auction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeHistory {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "`Key`")
        private Long id;

        /** 회원 */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "member_id", nullable = false)
        private Member member;

        /** 경매 */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "auction_id", nullable = false)
        private Auction auction;

        /** SELLER/BUYER */
        @Enumerated(EnumType.STRING)
        @Column(name = "type", nullable = false, length = 10)
        private TradeRole role;

        /** 생성 일시 */
        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;

        @PrePersist
        void onCreate() {
                LocalDateTime now = LocalDateTime.now();
                if (createdAt == null)
                        createdAt = now;
        }
}
