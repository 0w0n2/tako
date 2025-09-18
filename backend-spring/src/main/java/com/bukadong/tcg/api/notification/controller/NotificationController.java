package com.bukadong.tcg.api.notification.controller;

import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.api.notification.dto.response.NotificationListRow;
import com.bukadong.tcg.api.notification.dto.response.UnreadCountResponse;
import com.bukadong.tcg.api.notification.service.NotificationQueryService;
import com.bukadong.tcg.api.notification.service.NotificationReadService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.dto.PageResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;

/**
 * 알림 조회 컨트롤러
 * <P>
 * 내 알림 목록을 최신순으로 조회한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN BaseResponse<PageResponse<NotificationListRow>>
 */
@Tag(name = "Notifications", description = "알림 조회 API")
@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationReadService notificationReadService;
    private final MemberQueryService memberQueryService;

    /**
     * 내 알림 목록 조회
     * <P>
     * 로그인 사용자의 알림을 페이지로 반환한다.
     * </P>
     * 
     * @PARAM me 인증 사용자
     * @PARAM pageable 페이지/사이즈
     * @RETURN BaseResponse<PageResponse<NotificationListRow>>
     */
    @Operation(summary = "내 알림 목록", description = "로그인 사용자의 알림을 최신순으로 반환합니다.")
    @GetMapping
    public BaseResponse<PageResponse<NotificationListRow>> getMyList(
            @Parameter(description = "페이지 번호(0-base)") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        PageResponse<NotificationListRow> result = notificationQueryService.getMyNotifications(me.getId(), pageable);
        return BaseResponse.onSuccess(result);
    }

    /**
     * 단건 읽음 처리
     * <P>
     * idempotent. 이미 읽은 경우에도 성공 응답.
     * </P>
     */
    @Operation(summary = "알림 단건 읽음 처리", description = "지정한 알림을 읽음으로 표시합니다.")
    @PatchMapping("/{notificationId}/read")
    public BaseResponse<Void> readOne(
            @Parameter(name = "notificationId", description = "알림 ID", required = true) @PathVariable("notificationId") Long notificationId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        notificationReadService.markRead(me.getId(), notificationId);
        return BaseResponse.onSuccess();
    }

    /**
     * 내 모든 미읽음 일괄 읽음 처리
     * <P>
     * 변경 건수만 반환.
     * </P>
     */
    @Operation(summary = "알림 일괄 읽음", description = "내 모든 미읽음 알림을 읽음으로 표시합니다.")
    @PostMapping("/read-all")
    public BaseResponse<Integer> readAll(@AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        int changed = notificationReadService.markAllRead(me.getId());
        return BaseResponse.onSuccess(changed);
    }

    /**
     * 미읽음 카운트
     */
    @Operation(summary = "미읽음 카운트", description = "내 미읽은 알림 개수를 반환합니다.")
    @GetMapping("/unread-count")
    public BaseResponse<UnreadCountResponse> unreadCount(@AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        UnreadCountResponse res = notificationReadService.getUnreadCount(me.getId());
        return BaseResponse.onSuccess(res);
    }
}
