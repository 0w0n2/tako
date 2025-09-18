package com.bukadong.tcg.api.wish.entity;

import com.bukadong.tcg.global.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 카드 찜하기 엔티티
 * <p>
 * 회원이 특정 카드를 찜했는지 여부를 저장한다.
 * </p>
 * <ul>
 * <li>(member_id, card_id) 복합 고유 제약</li>
 * <li>member_id, card_id, (member_id, wish_flag) 인덱스</li>
 * </ul>
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "wish_card", uniqueConstraints = {
        @UniqueConstraint(name = "uk_wish_card_member_card", columnNames = { "member_id", "card_id" }) }, indexes = {
                @Index(name = "idx_wish_card_member", columnList = "member_id"),
                @Index(name = "idx_wish_card_card", columnList = "card_id"),
                @Index(name = "idx_wish_card_member_flag", columnList = "member_id,wish_flag") })
public class WishCard extends BaseEntity {

    /** 찜 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 카드 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false, foreignKey = @ForeignKey(name = "FK_wish_card_card"))
    private Long cardId;

    /** 회원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "FK_wish_card_member"))
    private Long memberId;

    /** 찜 여부 */
    @Column(name = "wish_flag", nullable = false, columnDefinition = "bit(1)")
    private boolean wishFlag;

    private WishCard(Long memberId, Long cardId, boolean wishFlag) {
        this.memberId = memberId;
        this.cardId = cardId;
        this.wishFlag = wishFlag;
    }

    /**
     * 신규 관심 등록 팩토리
     * <P>
     * 초기 wishFlag=true로 생성.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @PARAM cardId 카드 ID
     * @RETURN WishCard
     */
    public static WishCard create(Long memberId, Long cardId) {
        return new WishCard(memberId, cardId, true);
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
