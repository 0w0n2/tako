package com.bukadong.tcg.wish.entity;

import com.bukadong.tcg.auction.entity.Auction;
import com.bukadong.tcg.common.base.BaseEntity;
import com.bukadong.tcg.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 경매 찜하기 엔티티
 *
 * <p>
 * 회원이 특정 경매를 찜했는지 여부를 저장한다.
 * </p>
 *
 * <ul>
 * <li>(member_id, auction_id) 복합 고유 제약</li>
 * <li>member_id, auction_id, (member_id, wish_flag) 인덱스</li>
 * </ul>
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

    /** 찜 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 대상 경매 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id", nullable = false, foreignKey = @ForeignKey(name = "FK_wish_auction_auction"))
    private Auction auction;

    /** 회원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "FK_wish_auction_member"))
    private Member member;

    /** 찜 여부 */
    @Column(name = "wish_flag", nullable = false)
    private boolean wishFlag;
}
