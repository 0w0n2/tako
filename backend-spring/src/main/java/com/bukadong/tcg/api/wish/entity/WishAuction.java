package com.bukadong.tcg.api.wish.entity;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.global.common.base.BaseEntity;
import com.bukadong.tcg.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 경매 찜하기 엔티티
 * <p>
 * 회원이 특정 경매를 찜했는지 여부를 저장한다.
 * </p>
 * <ul>
 * <li>(member_id, auction_id) 복합 고유 제약</li>
 * <li>member_id, auction_id, (member_id, wish_flag) 인덱스</li>
 * </ul>
 */
@Entity
@Table(name = "wish_auction", uniqueConstraints = {
        @UniqueConstraint(name = "uk_wish_auction_member_auction", columnNames = { "member_id",
                "auction_id" }) }, indexes = { @Index(name = "idx_wish_auction_member", columnList = "member_id"),
                        @Index(name = "idx_wish_auction_auction", columnList = "auction_id"),
                        @Index(name = "idx_wish_auction_member_flag", columnList = "member_id,wish_flag") })
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
    @JoinColumn(name = "auction_id", nullable = false, foreignKey = @ForeignKey(name = "FK_wish_auction_auction"))
    private Long auctionId;

    /** 회원 */
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "FK_wish_auction_member"))
    private Long memberId;

    /** 찜 여부 */
    @Column(name = "wish_flag", nullable = false)
    private boolean wishFlag;

    private WishAuction(Long memberId, Long auctionId, boolean wishFlag) {
        this.memberId = memberId;
        this.auctionId = auctionId;
        this.wishFlag = wishFlag;
    }

    /**
     * 신규 경매 등록 팩토리
     * <P>
     * 초기 wishFlag=true로 생성.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @PARAM cardId 카드 ID
     * @RETURN WishAuction
     */
    public static WishAuction create(Long memberId, Long auctionId) {
        return new WishAuction(memberId, auctionId, true);
    }

    /**
     * 관심 켜기
     * <P>
     * 삭제 대신 플래그를 true로 복구한다.
     * </P>
     * 
     * @PARAM 없음
     * @RETURN 없음
     */
    public void enable() {
        this.wishFlag = true;
    }

    /**
     * 관심 끄기
     * <P>
     * 행 삭제 대신 플래그만 false로 변경한다.
     * </P>
     * 
     * @PARAM 없음
     * @RETURN 없음
     */
    public void disable() {
        this.wishFlag = false;
    }
}
