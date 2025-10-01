package com.bukadong.tcg.api.notification.service;

import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.api.notification.entity.Notification;
import com.bukadong.tcg.api.notification.entity.NotificationType;
import com.bukadong.tcg.api.notification.entity.NotificationTypeCode;
import com.bukadong.tcg.api.notification.repository.NotificationRepository;
import com.bukadong.tcg.api.notification.repository.NotificationTypeRepository;
import com.bukadong.tcg.api.notification.util.NotificationTargetUrlBuilder;
import com.bukadong.tcg.api.notification.event.NotificationCreatedEvent;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * 알림 생성 커맨드 서비스
 * <P>
 * 컨트롤러/도메인 이벤트에서 호출되어 알림을 저장한다. 컨트롤러에서 기본 검증(@Validated)이 끝났다고 가정하고, 이 서비스에서는
 * DB 의존 검증만 수행한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Service
@RequiredArgsConstructor
public class NotificationCommandService {

    private static final Logger log = LoggerFactory.getLogger(NotificationCommandService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationTargetUrlBuilder targetUrlBuilder;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 공통 생성 로직
     * <P>
     * typeCode로 NotificationType을 조회한 뒤 알림을 저장한다.
     * </P>
     * 
     * @PARAM memberId 수신자
     * @PARAM typeCode 알림 타입 코드
     * @PARAM causeId 원인 리소스 ID(경매/카드/문의 등)
     * @PARAM title 제목
     * @PARAM message 본문
     * @RETURN 생성된 Notification ID
     */
    public Long create(Long memberId, NotificationTypeCode typeCode, Long causeId, String title, String message) {

        // FK 보호: 수신자 없으면 기록 스킵 (경고만)
        if (memberId == null || !memberRepository.existsById(memberId)) {
            // 로컬/개발 데이터 불일치 방지용 가드
            log.warn("Skip notification create: invalid memberId={} type={} causeId={}", memberId, typeCode, causeId);
            return null;
        }

        NotificationType type = notificationTypeRepository.findByCode(typeCode)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOTIFICATION_NOT_FOUND));

        String targetUrl = buildTargetUrl(typeCode, causeId);

        Notification n = Notification.builder().memberId(memberId).type(type).causeId(causeId).title(title)
                .message(message).targetUrl(targetUrl).build();

        // IDENTITY 전략에서 지연 식별자 할당으로 인한 null 방지
        Long id = notificationRepository.saveAndFlush(n).getId();
        if (id == null) {
            log.warn("Notification saved but id is null. Skip event publish. memberId={} type={} causeId={}", memberId,
                    typeCode, causeId);
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("Notification created: id={} memberId={} type={} causeId={} title={}", id, memberId, typeCode,
                    causeId, title);
        }

        // 트랜잭션 커밋 후 FCM 발송을 위해 이벤트 발행
        eventPublisher.publishEvent(new NotificationCreatedEvent(id, memberId, typeCode, causeId, title, message,
                targetUrl));

        return id;
    }

    // ===== 편의 메서드(도메인 이벤트 별 메시지 템플릿) =====

    /**
     * 위시 경매 시작
     * <P>
     * memberId의 위시 상태인 경매가 시작되었을 때 호출한다.
     * </P>
     * 
     * @PARAM memberId 수신자
     * @PARAM auctionId 경매 ID
     * @PARAM extras 템플릿 바인딩 데이터(name, price 등)
     * @RETURN Notification ID
     */
    @Transactional
    public Long notifyWishAuctionStarted(Long memberId, Long auctionId, Map<String, Object> extras) {
        String title = "관심 경매가 시작되었습니다";
        String message = "위시한 경매가 방금 시작되었어요.";
        return create(memberId, NotificationTypeCode.WISH_AUCTION_STARTED, auctionId, title, message);
    }

    /**
     * 위시 경매 마감 임박
     */
    @Transactional
    public Long notifyWishAuctionDueSoon(Long memberId, Long auctionId, Map<String, Object> extras) {
        String title = "관심 경매가 곧 마감됩니다";
        String message = "마감 전에 확인해 보세요.";
        return create(memberId, NotificationTypeCode.WISH_AUCTION_DUE_SOON, auctionId, title, message);
    }

    /**
     * 위시 경매 마감됨
     */
    @Transactional
    public Long notifyWishAuctionEnded(Long memberId, Long auctionId, Map<String, Object> extras) {
        String title = "관심 경매가 마감되었습니다";
        String message = "결과를 확인해 보세요.";
        return create(memberId, NotificationTypeCode.WISH_AUCTION_ENDED, auctionId, title, message);
    }

    /**
     * 위시 카드가 경매에 등록됨
     */
    @Transactional
    public Long notifyWishCardListed(Long memberId, Long cardId, Map<String, Object> extras) {
        String title = "관심 카드가 경매에 등록되었어요";
        String message = "놓치지 않도록 지금 확인해 보세요.";
        return create(memberId, NotificationTypeCode.WISH_CARD_LISTED, cardId, title, message);
    }

    /**
     * 내 경매에 문의 생성
     */
    @Transactional
    public Long notifyAuctionNewInquiry(Long memberId, Long auctionId, Map<String, Object> extras) {
        String title = "내 경매에 문의가 달렸습니다";
        String message = "새 문의를 확인해 주세요.";
        return create(memberId, NotificationTypeCode.AUCTION_NEW_INQUIRY, auctionId, title, message);
    }

    /**
     * 내가 남긴 문의에 답글 달림
     */
    @Transactional
    public Long notifyInquiryAnswered(Long inquirerId, Long inquiryId, Map<String, Object> extras) {
        String title = "문의에 답변이 등록되었습니다";
        String message = "답변 내용을 확인해 보세요.";
        return create(inquirerId, NotificationTypeCode.INQUIRY_ANSWERED, inquiryId, title, message);
    }
    // ------------------ 경매 종료 관련 (낙찰/종료/취소) ------------------

    /** 낙찰자에게: 경매 낙찰 */
    @Transactional
    public Long notifyAuctionWon(Long memberId, Long auctionId, BigDecimal amount, Instant closedAt) {
        String title = "경매에 낙찰되었습니다";
        String message = "축하합니다! 해당 경매의 낙찰자로 선정되었어요. 낙찰가: " + safeAmount(amount) + "\n받는 배송지를 등록/선택해 주세요.";
        return create(memberId, NotificationTypeCode.AUCTION_WON, auctionId, title, message);
    }

    /** 판매자에게: 경매 종료(낙찰) */
    @Transactional
    public Long notifyAuctionSellerClosed(Long memberId, Long auctionId, BigDecimal amount, Instant closedAt) {
        String title = "내 경매가 종료되었습니다";
        String message = "경매가 종료되어 낙찰이 확정되었습니다. 낙찰가: " + safeAmount(amount);
        return create(memberId, NotificationTypeCode.AUCTION_CLOSED_SELLER, auctionId, title, message);
    }

    /** 구매자에게: 즉시구매 완료 */
    @Transactional
    public Long notifyBuyNowBuyer(Long buyerId, Long auctionId, BigDecimal amount, Instant closedAt) {
        String title = "즉시구매가 완료되었습니다";
        String message = "즉시구매가 성공적으로 처리되었어요. 결제 금액: " + safeAmount(amount) + "\n배송지 등록/선택을 진행해 주세요.";
        // 스키마 변경 없이 AUCTION_WON 타입 재사용
        return create(buyerId, NotificationTypeCode.AUCTION_WON, auctionId, title, message);
    }

    /** 판매자에게: 즉시구매로 판매 완료 */
    @Transactional
    public Long notifyBuyNowSeller(Long sellerId, Long auctionId, BigDecimal amount, Instant closedAt) {
        String title = "즉시구매로 판매가 완료되었어요";
        String message = "해당 경매가 즉시구매로 즉시 종료되었습니다. 판매가: " + safeAmount(amount);
        // 스키마 변경 없이 AUCTION_CLOSED_SELLER 타입 재사용
        return create(sellerId, NotificationTypeCode.AUCTION_CLOSED_SELLER, auctionId, title, message);
    }

    /** 판매자에게: 관리자 강제 종료 알림 */
    @Transactional
    public Long notifyAdminCanceled(Long memberId, Long auctionId) {
        String title = "경매가 관리자에 의해 종료되었습니다";
        String message = "운영자 정책에 따라 경매가 종료되었어요. 자세한 사유는 고객센터를 확인해 주세요.";
        return create(memberId, NotificationTypeCode.AUCTION_CANCELED, auctionId, title, message);
    }

    /** 판매자에게: 사용자(본인) 취소로 종료 알림 */
    @Transactional
    public Long notifySellerCanceled(Long memberId, Long auctionId) {
        String title = "경매가 취소되었습니다";
        String message = "요청하신 취소 처리로 경매가 종료되었습니다.";
        return create(memberId, NotificationTypeCode.AUCTION_CANCELED, auctionId, title, message);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long notifyAuctionSellerClosedUnsold(Long sellerId, Long auctionId) {
        String title = "내 경매가 유찰되었습니다";
        String message = "입찰이 없어 경매가 종료되었어요.";
        // 타입은 스키마 변경 없이 AUCTION_CLOSED_SELLER 재사용
        return create(sellerId, NotificationTypeCode.AUCTION_CLOSED_SELLER, auctionId, title, message);
    }

    // ============================ 유틸 ============================

    private String buildTargetUrl(NotificationTypeCode typeCode, Long causeId) {
        return switch (typeCode) {
            case WISH_AUCTION_STARTED, WISH_AUCTION_DUE_SOON, WISH_AUCTION_ENDED, AUCTION_NEW_INQUIRY, AUCTION_WON,
                    AUCTION_CLOSED_SELLER, AUCTION_CANCELED, DELIVERY_STARTED, DELIVERY_STATUS_CHANGED,
                    DELIVERY_CONFIRM_REQUEST, DELIVERY_CONFIRMED_SELLER, BID_ACCEPTED, BID_REJECTED, BID_FAILED ->
                targetUrlBuilder
                        .buildForAuction(causeId);
            case WISH_CARD_LISTED -> targetUrlBuilder.buildForCard(causeId);
            case INQUIRY_ANSWERED -> targetUrlBuilder.buildForInquiry(causeId);
            case NOTICE_NEW -> "/notice/" + (causeId == null ? "" : causeId);
            case BID_OUTBID -> targetUrlBuilder.buildForAuction(causeId);
        };
    }

    private String safeAmount(BigDecimal amount) {
        return (amount == null) ? "-" : amount.toPlainString();
    }

    // ================= 배송 관련 편의 메서드 =================

    /** 운송장 등록 등으로 배송이 시작되었을 때(구매자) */
    @Transactional
    public Long notifyDeliveryStarted(Long buyerId, Long auctionId, String auctionTitle) {
        String shortTitle = safeShortTitle(auctionTitle);
        String title = shortTitle + " 배송이 시작되었어요";
        String message = "판매자가 운송장 번호를 등록했어요. 배송이 시작되었습니다.";
        return create(buyerId, NotificationTypeCode.DELIVERY_STARTED, auctionId, title, message);
    }

    /** 배송 상태 변경(구매자) */
    @Transactional
    public Long notifyDeliveryStatusChanged(Long buyerId, Long auctionId, String auctionTitle, String newStatusLabel) {
        String shortTitle = safeShortTitle(auctionTitle);
        String title = shortTitle + " 배송 상태 변경";
        String message = shortTitle + " 경매의 배송상태가 " + newStatusLabel + "(으)로 변경되었습니다.";
        return create(buyerId, NotificationTypeCode.DELIVERY_STATUS_CHANGED, auctionId, title, message);
    }

    /** 배송 완료 시 구매 확정 요청(구매자) */
    @Transactional
    public Long notifyDeliveryConfirmRequest(Long buyerId, Long auctionId, String auctionTitle) {
        String shortTitle = safeShortTitle(auctionTitle);
        String title = shortTitle + " 구매 확정 요청";
        String message = "상품을 받으셨다면 구매 확정을 진행해 주세요.";
        return create(buyerId, NotificationTypeCode.DELIVERY_CONFIRM_REQUEST, auctionId, title, message);
    }

    /** 구매자가 구매확정 시 판매자에게 알림 */
    @Transactional
    public Long notifySaleConfirmedToSeller(Long sellerId, Long auctionId, String auctionTitle) {
        String shortTitle = safeShortTitle(auctionTitle);
        String title = shortTitle + " 판매 확정 완료";
        String message = "구매자가 구매 확정을 완료했어요. 정산이 진행됩니다.";
        return create(sellerId, NotificationTypeCode.DELIVERY_CONFIRMED_SELLER, auctionId, title, message);
    }

    private String safeShortTitle(String title) {
        if (title == null)
            return "";
        int len = Math.min(title.length(), 10);
        return title.substring(0, len);
    }

    // ================= 입찰 결과 편의 메서드 =================

    /** 입찰 반영 성공(VALID) */
    @Transactional
    public Long notifyBidAccepted(Long memberId, Long auctionId, BigDecimal amount) {
        String title = "입찰 성공";
        String message = "입찰이 반영되었습니다. 금액: " + safeAmount(amount);
        return create(memberId, NotificationTypeCode.BID_ACCEPTED, auctionId, title, message);
    }

    /** 입찰 거절(REJECTED) */
    @Transactional
    public Long notifyBidRejected(Long memberId, Long auctionId, BigDecimal amount, String reasonCode) {
        String title = "입찰 거절";
        String reason = (reasonCode == null ? "조건 미충족" : reasonCode);
        String message = "입찰이 반영되지 않았습니다. 금액: " + safeAmount(amount) + " (사유: " + reason + ")";
        return create(memberId, NotificationTypeCode.BID_REJECTED, auctionId, title, message);
    }

    /** 입찰 시스템 오류(FAILED) */
    @Transactional
    public Long notifyBidFailed(Long memberId, Long auctionId, BigDecimal amount, String reasonCode) {
        String title = "입찰 처리 오류";
        String code = (reasonCode == null ? "SYSTEM" : reasonCode);
        String message = "시스템 오류로 입찰 반영에 실패했습니다. 금액: " + safeAmount(amount) + " (코드: " + code + ")";
        return create(memberId, NotificationTypeCode.BID_FAILED, auctionId, title, message);
    }

    /** 상위 입찰 발생으로 최고가 지위 상실 */
    @Transactional
    public Long notifyBidOutbid(Long memberId, Long auctionId, BigDecimal newTopAmount) {
        String title = "상위 입찰이 발생했습니다";
        String message = "내 입찰이 최고가가 아닙니다. 현재가: " + safeAmount(newTopAmount);
        return create(memberId, NotificationTypeCode.BID_OUTBID, auctionId, title, message);
    }
}
