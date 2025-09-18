package com.bukadong.tcg.api.notification.service;

import com.bukadong.tcg.api.notification.dto.response.UnreadCountResponse;
import com.bukadong.tcg.api.notification.entity.Notification;
import com.bukadong.tcg.api.notification.repository.NotificationRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 알림 읽음 처리 서비스
 * <P>
 * 단건/일괄 읽음 및 미읽음 카운트.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Service
@RequiredArgsConstructor
public class NotificationReadService {

    private final NotificationRepository notificationRepository;

    /**
     * 단건 읽음 처리
     * <P>
     * 소유자 검증 후 읽음 표시. 이미 읽은 경우에도 idempotent.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @PARAM notificationId 알림 ID
     * @RETURN 없음
     */
    @Transactional
    public void markRead(Long memberId, Long notificationId) {
        Notification n = notificationRepository.findByIdAndMemberId(notificationId, memberId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
        if (!n.isRead()) {
            n.markRead(LocalDateTime.now());
        }
    }

    /**
     * 내 모든 미읽음 알림 일괄 읽음
     * <P>
     * 벌크 업데이트 사용.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @RETURN 변경 건수
     */
    @Transactional
    public int markAllRead(Long memberId) {
        return notificationRepository.markAllReadByMemberId(memberId, LocalDateTime.now());
    }

    /**
     * 미읽음 카운트
     * 
     * @PARAM memberId 회원 ID
     * @RETURN UnreadCountResponse
     */
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long memberId) {
        long cnt = notificationRepository.countByMemberIdAndReadIsFalse(memberId);
        return new UnreadCountResponse(cnt);
    }
}
