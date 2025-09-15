package com.bukadong.tcg.api.auction.entity;

import com.bukadong.tcg.api.auction.converter.AuctionBidUnitConverter;
import com.bukadong.tcg.api.card.entity.Card;
import com.bukadong.tcg.api.card.entity.CardAiGrade;
import com.bukadong.tcg.api.card.entity.PhysicalCard;
import com.bukadong.tcg.api.category.entity.CategoryMajor;
import com.bukadong.tcg.api.category.entity.CategoryMedium;
import com.bukadong.tcg.global.common.base.BaseEntity;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.api.delivery.entity.Delivery;
import com.bukadong.tcg.api.member.entity.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 경매 엔티티.
 * <p>
 * 회원이 보유한 실물 카드를 경매로 등록하고, 시작가/현재가/입찰단위/기간/옵션 카드/카테고리 메타정보을 관리한다.
 * </p>
 * <p>
 * 스키마 메타데이터:
 * </p>
 * <ul>
 * <li>TABLE: 경매</li>
 * <li>UK: code</li>
 * <li>INDEX: member_id, delivery_id, physical_card_id, card_id,
 * category_major_id, category_medium_id, (start_datetime,end_datetime)</li>
 * </ul>
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
@Setter
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
    @JoinColumn(name = "delivery_id", unique = true, // 하나의 배송은 하나의 경매와만 연결
            foreignKey = @ForeignKey(name = "FK_auction_delivery"))
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
    @Lob // 중요: MySQL에서 LONGTEXT를 원하면 columnDefinition을 명시
    @Column(name = "detail", nullable = false, columnDefinition = "LONGTEXT")
    private String detail;

    /** 시작 가격 (코인 단위, 소수점 8자리) */
    @NotNull
    @DecimalMin(value = "0.00000000")
    @Digits(integer = 12, fraction = 8) // precision=20,scale=8 중 비즈니스상 안전범위 검증
    @Column(name = "start_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal startPrice;

    /** 현재가 (코인 단위, 소수점 8자리) */
    @NotNull
    @DecimalMin(value = "0.00000000")
    @Digits(integer = 12, fraction = 8)
    @Column(name = "current_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal currentPrice;

    /** 입찰 단위 (예: 0.01/0.1/0.3/0.5/1/5/10) */
    @NotNull
    @Convert(converter = AuctionBidUnitConverter.class)
    @Column(name = "bid_unit", nullable = false, length = 10)
    private AuctionBidUnit bidUnit;

    /** 시작 일시 */
    @NotNull
    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    /** 종료 일시 (시작보다 뒤) */
    @NotNull
    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;

    /** 경매 기간(일) */
    @NotNull
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

    /**
     * 저장 전 훅.
     * <p>
     * 중요 로직: currentPrice 미설정 시 startPrice로 초기화한다.
     * </p>
     */
    @PrePersist
    void prePersistAuction() {
        // 중요: currentPrice가 null이면 startPrice로 초기화
        if (currentPrice == null && startPrice != null) {
            currentPrice = startPrice;
        }
        // 기본값 미지정 필드 초기 보정
        if (durationDays == null)
            durationDays = 1;
        if (!extensionFlag) {
            /* no-op: 명시 플래그 유지 */ }
    }

    /**
     * 업데이트 전 훅.
     * <p>
     * 중요 로직: 종료일시가 시작일시보다 빠르면 안 된다.
     * </p>
     */
    @PreUpdate
    void preUpdateAuction() {
        // 중요한 무결성 체크(서비스/검증단에서도 보통 같이 체크)
        if (startDatetime != null && endDatetime != null && endDatetime.isBefore(startDatetime)) {
            throw new BaseException(BaseResponseStatus.AUCTION_DATE_INVALID);
        }
    }

    /**
     * 경매가 주어진 시각에 진행 중인지 여부
     * <P>
     * isEnd=false 이고, startDatetime <= when <= endDatetime 이면 true.
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
        if (this.getStartDatetime() != null && when.isBefore(this.getStartDatetime()))
            return false;
        if (this.getEndDatetime() != null && when.isAfter(this.getEndDatetime()))
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
        return (when != null && this.getEndDatetime() != null && when.isAfter(this.getEndDatetime()));
    }
}
