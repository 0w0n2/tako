package com.bukadong.tcg.api.trade.entity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 거래내역 엔티티
 * <p>
 * 경매 체결 후, 회원별 거래 참여 이력을 기록한다.
 * </p>
 * <ul>
 * <li>(auction_id, type) 복합 고유 제약</li>
 * <li>member_id, auction_id 각각 인덱스 생성</li>
 * </ul>
 */
@Entity
@Table(name = "trade_history", uniqueConstraints = {
        @UniqueConstraint(name = "uk_trade_auction_type", columnNames = { "auction_id", "type" }) }, indexes = {
                @Index(name = "idx_trade_member", columnList = "member_id"),
                @Index(name = "idx_trade_auction", columnList = "auction_id") })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeHistory {

    /** 거래내역 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 회원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "FK_trade_history_member"))
    private Member member;

    /** 경매 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id", nullable = false, foreignKey = @ForeignKey(name = "FK_trade_history_auction"))
    private Auction auction;

    /** SELLER / BUYER */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private TradeRole role;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneOffset.UTC);
        }
    }
}
