package com.bukadong.tcg.api.auction.entity;

import com.bukadong.tcg.api.bid.converter.AuctionBidUnitConverter;
import com.bukadong.tcg.api.bid.entity.AuctionBidUnit;
import com.bukadong.tcg.api.card.entity.Card;
import com.bukadong.tcg.api.card.entity.CardAiGrade;
import com.bukadong.tcg.api.card.entity.PhysicalCard;
import com.bukadong.tcg.api.category.entity.CategoryMajor;
import com.bukadong.tcg.api.category.entity.CategoryMedium;
import com.bukadong.tcg.api.delivery.entity.Delivery;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.base.BaseEntity;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 경매 엔티티.
 * <P>
 * 회원이 보유한 실물 카드를 경매로 등록하고, 시작가/현재가/입찰단위/기간/옵션 카드/카테고리 메타정보을 관리한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Entity
@Table(name = "auction", uniqueConstraints = {
        @UniqueConstraint(name = "uk_auction_code", columnNames = { "code" }) }, indexes = {
                @Index(name = "idx_auc_owner", columnList = "member_id"),
                @Index(name = "idx_auc_delivery", columnList = "delivery_id"),
                @Index(name = "idx_auc_pcard", columnList = "physical_card_id"),
                @Index(name = "idx_auc_card", columnList = "card_id"),
                @Index(name = "idx_auc_cat_major", columnList = "category_major_id"),
                @Index(name = "idx_auc_cat_medium", columnList = "category_medium_id"),
                @Index(name = "idx_auc_start_end", columnList = "start_datetime,end_datetime") })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Auction extends BaseEntity {

    /** PK */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 경매 생성 회원 (필수) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    /** 배송 (1:1 매핑) */
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "delivery_id", unique = true, foreignKey = @ForeignKey(name = "FK_auction_delivery"))
    private Delivery delivery;

    /** 경매 대상 실물 카드 (옵션) -> physical_card_id */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "physical_card_id", nullable = true, foreignKey = @ForeignKey(name = "FK_auction_physical_card"))
    private PhysicalCard physicalCard;

    /** 카드 메타 (필수) -> card_id */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false, foreignKey = @ForeignKey(name = "FK_auction_card"))
    private Card card;

    /** 카테고리 대분류 (필수) -> category_major_id */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_major_id", nullable = false, foreignKey = @ForeignKey(name = "FK_auction_category_major"))
    private CategoryMajor categoryMajor;

    /** 카테고리 중분류 (필수) -> category_medium_id */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_medium_id", nullable = false, foreignKey = @ForeignKey(name = "FK_auction_category_medium"))
    private CategoryMedium categoryMedium;

    /** 카드 AI 등급 FK -> grade_id */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false, foreignKey = @ForeignKey(name = "FK_auction_card_ai_grade"))
    private CardAiGrade grade;

    /** 경매 코드 (외부 식별자) */
    @Size(min = 1, max = 40)
    @Column(name = "code", nullable = false, length = 40)
    private String code;

    /** 경매 제목 */
    @Size(min = 1, max = 40)
    @Column(name = "title", nullable = false, length = 40)
    private String title;

    /** 경매 상세 설명 (MySQL LONGTEXT) */
    @Lob
    @Column(name = "detail", nullable = false, columnDefinition = "LONGTEXT")
    private String detail;

    /** 시작 가격 (코인 단위, 소수점 8자리) */
    @NotNull(message = "시작 가격은 필수입니다.")
    @DecimalMin(value = "0.00000000")
    @Digits(integer = 12, fraction = 8)
    @Column(name = "start_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal startPrice;

    /** 현재가 (코인 단위, 소수점 8자리) */
    @NotNull(message = "현재가는 필수입니다.")
    @DecimalMin(value = "0.00000000")
    @Digits(integer = 12, fraction = 8)
    @Column(name = "current_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal currentPrice;

    /** 입찰 단위 (예: 0.01/0.1/0.3/0.5/1/5/10) */
    @NotNull(message = "입찰 단위는 필수입니다.")
    @Convert(converter = AuctionBidUnitConverter.class)
    @Column(name = "bid_unit", nullable = false, length = 10)
    private AuctionBidUnit bidUnit;

    /** 시작 일시 */
    @NotNull(message = "시작 일시는 필수입니다.")
    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    /** 종료 일시 (시작보다 뒤) */
    @NotNull(message = "종료 일시는 필수입니다.")
    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;

    /** 경매 기간(일) */
    @NotNull(message = "경매 기간은 필수입니다.")
    @Min(1)
    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    /** 종료 여부 (false=진행중, true=종료됨) */
    @Column(name = "is_end", nullable = false)
    private boolean isEnd;

    /** 즉시 구매 가능 여부 */
    @Column(name = "buy_now_flag", nullable = false)
    private boolean buyNowFlag;

    /** 즉시 구매 가격 (옵션) */
    @DecimalMin(value = "0.00000000")
    @Digits(integer = 12, fraction = 8)
    @Column(name = "buy_now_price", precision = 20, scale = 8)
    private BigDecimal buyNowPrice;

    /** 마감 임박 연장 가능 여부 */
    @Column(name = "extension_flag", nullable = false)
    private boolean extensionFlag;

    /** 세금 포함 여부 */
    @Column(name = "tax_flag", nullable = false)
    private boolean taxFlag;

    /* =====================[ 종료 처리 관련 신규 필드 ]===================== */

    /** 종료 사유 */
    @Enumerated(EnumType.STRING)
    @Column(name = "close_reason", length = 20)
    private AuctionCloseReason closeReason;

    /** 종료 시각 */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    /** 낙찰자 회원 ID */
    @Column(name = "winner_member_id")
    private Long winnerMemberId;

    /** 낙찰 입찰 ID */
    @Column(name = "winner_bid_id")
    private Long winnerBidId;

    /** 낙찰 금액 (코인 단위, 소수점 8자리) */
    @Digits(integer = 12, fraction = 8)
    @Column(name = "winning_amount", precision = 20, scale = 8)
    private BigDecimal winningAmount;

    /* ==============================[ Hooks ]============================== */

    /**
     * 저장 전 훅.
     * <P>
     * currentPrice 미설정 시 startPrice로 초기화한다.
     * </P>
     * 
     * @RETURN 없음
     */
    @PrePersist
    void prePersistAuction() {
        if (currentPrice == null && startPrice != null) {
            currentPrice = startPrice;
        }
        if (durationDays == null) {
            durationDays = 1;
        }
        // extensionFlag/taxFlag/buyNowFlag 등은 요청 값 유지(no-op)
    }

    /**
     * 업데이트 전 훅.
     * <P>
     * 종료일시가 시작일시보다 빠르면 예외.
     * </P>
     * 
     * @RETURN 없음
     */
    @PreUpdate
    void preUpdateAuction() {
        if (startDatetime != null && endDatetime != null && endDatetime.isBefore(startDatetime)) {
            throw new BaseException(BaseResponseStatus.AUCTION_DATE_INVALID);
        }
    }

    /* ===========================[ 도메인 로직 ]=========================== */

    /**
     * 경매가 주어진 시각에 진행 중인지 여부
     * <P>
     * isEnd=false 이고, startDatetime ≤ when ≤ endDatetime 이면 true.
     * </P>
     * 
     * @PARAM when 기준 시각(Null 허용하지 않음)
     * @RETURN 진행 중이면 true
     */
    public boolean isRunningAt(LocalDateTime when) {
        if (when == null)
            return false;
        if (this.isEnd)
            return false;
        if (this.startDatetime != null && when.isBefore(this.startDatetime))
            return false;
        if (this.endDatetime != null && when.isAfter(this.endDatetime))
            return false;
        return true;
    }

    /**
     * 경매가 이미 종료 상태인지 여부
     * <P>
     * isEnd=true 또는 when이 endDatetime 이후이면 종료로 간주.
     * </P>
     * 
     * @PARAM when 기준 시각(Null 허용)
     * @RETURN 종료면 true
     */
    public boolean isEndedAt(LocalDateTime when) {
        if (this.isEnd)
            return true;
        return (when != null && this.endDatetime != null && when.isAfter(this.endDatetime));
    }

    /**
     * 현재 가격 변경
     * <P>
     * 음수/하향 변경 금지.
     * </P>
     * 
     * @PARAM newPrice 새 가격
     * @RETURN 없음
     */
    public void changeCurrentPrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0 || this.currentPrice.compareTo(newPrice) > 0) {
            throw new BaseException(BaseResponseStatus.AUCTION_CONFLICT);
        }
        this.currentPrice = newPrice;
    }

    /**
     * 종료 일시 설정
     * <P>
     * 외부에서 종료 시각 조정이 필요한 경우 사용.
     * </P>
     * 
     * @PARAM localDateTime 종료 일시
     * @RETURN 없음
     */
    public void setEndDatetime(LocalDateTime localDateTime) {
        this.endDatetime = localDateTime;
    }

    /**
     * 종료 가능 여부
     * <P>
     * 이미 종료가 아니고, endDatetime이 현재 이전이면 종료 가능.
     * </P>
     * 
     * @RETURN 종료 가능 여부
     */
    public boolean isClosableNow() {
        return !this.isEnd && this.endDatetime != null && this.endDatetime.isBefore(LocalDateTime.now(ZoneOffset.UTC));
    }

    /**
     * 경매 종료 마킹
     * <P>
     * 중복 종료 요청은 무시(idempotent).
     * </P>
     * 
     * @PARAM reason 종료 사유
     * @PARAM closedAt 종료 시각
     * @RETURN 없음
     */
    public void markClosed(AuctionCloseReason reason, LocalDateTime closedAt) {
        if (this.isEnd) {
            return;
        }
        this.isEnd = true;
        this.closeReason = reason;
        this.closedAt = closedAt;
    }

    /**
     * 낙찰 정보 설정
     * <P>
     * 낙찰자/입찰/금액을 기록한다.
     * </P>
     * 
     * @PARAM memberId 낙찰자 회원 ID
     * @PARAM bidId 낙찰 입찰 ID
     * @PARAM amount 낙찰 금액
     * @RETURN 없음
     */
    public void setWinner(Long memberId, Long bidId, BigDecimal amount) {
        this.winnerMemberId = memberId;
        this.winnerBidId = bidId;
        this.winningAmount = amount;
    }
}
