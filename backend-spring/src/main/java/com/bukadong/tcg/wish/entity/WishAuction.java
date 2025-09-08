package com.bukadong.tcg.wish.entity;

import com.bukadong.tcg.auction.entity.Auction;
import com.bukadong.tcg.common.base.BaseEntity;
import com.bukadong.tcg.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 경매 찜하기
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_wish_auction_member_auction : (member_id, auction_id)
 * 복합 고유 제약 생성
 * - @Index idx_wish_auction_member : member_id 인덱스 생성
 * - @Index idx_wish_auction_auction : auction_id 인덱스 생성
 * - @Index idx_wish_auction_member_flag : (member_id, wish_flag) 복합 인덱스 생성
 */
@Entity
@Table(name = "wish_auction", uniqueConstraints = {
        @UniqueConstraint(name = "uk_wish_auction_member_auction", columnNames = { "member_id", "auction_id" })
}, indexes = {
        @Index(name = "idx_wish_auction_member", columnList = "member_id"),
        @Index(name = "idx_wish_auction_auction", columnList = "auction_id"),
        @Index(name = "idx_wish_auction_member_flag", columnList = "member_id,wish_flag")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishAuction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 대상 경매 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    /** 회원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 찜 여부 */
    @Column(name = "wish_flag", nullable = false)
    private boolean wishFlag;
}
