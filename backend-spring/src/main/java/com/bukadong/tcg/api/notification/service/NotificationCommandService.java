package com.bukadong.tcg.api.notification.service;

import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.api.notification.entity.Notification;
import com.bukadong.tcg.api.notification.entity.NotificationType;
import com.bukadong.tcg.api.notification.entity.NotificationTypeCode;
import com.bukadong.tcg.api.notification.repository.NotificationRepository;
import com.bukadong.tcg.api.notification.repository.NotificationTypeRepository;
import com.bukadong.tcg.api.notification.util.NotificationTargetUrlBuilder;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import lombok.RequiredArgsConstructor;
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

    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationTargetUrlBuilder targetUrlBuilder;
    private final MemberRepository memberRepository;

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
    @Transactional
    public Long create(Long memberId, NotificationTypeCode typeCode, Long causeId, String title, String message) {

        // FK 보호: 수신자 없으면 기록 스킵 (경고만)
        if (memberId == null || !memberRepository.existsById(memberId)) {
            // 로컬/개발 데이터 불일치 방지용 가드
            return null;
        }

        NotificationType type = notificationTypeRepository.findByCode(typeCode)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOTIFICATION_NOT_FOUND));

        String targetUrl = buildTargetUrl(typeCode, causeId);

        Notification n = Notification.builder().memberId(memberId).type(type).causeId(causeId).title(title)
                .message(message).targetUrl(targetUrl) // 현재는 ""
                .build();

        return notificationRepository.save(n).getId();
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
        String message = "축하합니다! 해당 경매의 낙찰자로 선정되었어요. 낙찰가: " + safeAmount(amount);
        return create(memberId, NotificationTypeCode.AUCTION_WON, auctionId, title, message);
    }

    /** 판매자에게: 경매 종료(낙찰) */
    @Transactional
    public Long notifyAuctionSellerClosed(Long memberId, Long auctionId, BigDecimal amount, Instant closedAt) {
        String title = "내 경매가 종료되었습니다";
        String message = "경매가 종료되어 낙찰이 확정되었습니다. 낙찰가: " + safeAmount(amount);
        return create(memberId, NotificationTypeCode.AUCTION_CLOSED_SELLER, auctionId, title, message);
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
        case WISH_AUCTION_STARTED, WISH_AUCTION_DUE_SOON, WISH_AUCTION_ENDED, AUCTION_NEW_INQUIRY, AUCTION_WON, AUCTION_CLOSED_SELLER, AUCTION_CANCELED -> targetUrlBuilder
                .buildForAuction(causeId);
        case WISH_CARD_LISTED -> targetUrlBuilder.buildForCard(causeId);
        case INQUIRY_ANSWERED -> targetUrlBuilder.buildForInquiry(causeId);
        };
    }

    private String safeAmount(BigDecimal amount) {
        return (amount == null) ? "-" : amount.toPlainString();
    }
}
