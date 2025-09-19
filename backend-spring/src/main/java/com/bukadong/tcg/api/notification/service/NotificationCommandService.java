package com.bukadong.tcg.api.notification.service;

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
import org.springframework.transaction.annotation.Transactional;

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

        NotificationType type = notificationTypeRepository.findByCode(typeCode)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOTIFICATION_NOT_FOUND));

        // URL 정책: 지금은 빈 문자열. 타입별 분기만 미리 구성.
        String targetUrl = switch (typeCode) {
        case WISH_AUCTION_STARTED, WISH_AUCTION_DUE_SOON, WISH_AUCTION_ENDED, AUCTION_NEW_INQUIRY -> targetUrlBuilder
                .buildForAuction(causeId);
        case WISH_CARD_LISTED -> targetUrlBuilder.buildForCard(causeId);
        case INQUIRY_ANSWERED -> targetUrlBuilder.buildForInquiry(causeId);
        };

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
    public Long notifyAuctionNewInquiry(Long ownerId, Long auctionId, Map<String, Object> extras) {
        String title = "내 경매에 문의가 달렸습니다";
        String message = "새 문의를 확인해 주세요.";
        return create(ownerId, NotificationTypeCode.AUCTION_NEW_INQUIRY, auctionId, title, message);
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
}
