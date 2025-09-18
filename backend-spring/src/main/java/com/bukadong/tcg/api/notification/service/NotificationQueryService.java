package com.bukadong.tcg.api.notification.service;

import com.bukadong.tcg.api.notification.dto.response.NotificationListRow;
import com.bukadong.tcg.api.notification.entity.Notification;
import com.bukadong.tcg.api.notification.repository.NotificationRepository;
import com.bukadong.tcg.global.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 조회 서비스
 * <P>
 * 내 알림 목록 페이지 조회. 정렬은 최신순(id DESC).
 * </P>
 * 
 * @PARAM 없음
 * @RETURN PageResponse<NotificationListRow>
 */
@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    /**
     * 내 알림 목록
     * <P>
     * memberId 기준으로 페이지 조회한다.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @PARAM pageable 페이지/사이즈/정렬
     * @RETURN PageResponse<NotificationListRow>
     */
    @Transactional(readOnly = true)
    public PageResponse<NotificationListRow> getMyNotifications(Long memberId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByMemberIdOrderByIdDesc(memberId, pageable);

        Page<NotificationListRow> mapped = page
                .map(n -> new NotificationListRow(n.getId(), n.getType().getCode().name(), n.getCauseId(), n.getTitle(),
                        n.getMessage(), n.getTargetUrl(), n.isRead(), n.getReadAt(), n.getCreatedAt()));

        return PageResponse.from(mapped);
    }
}
