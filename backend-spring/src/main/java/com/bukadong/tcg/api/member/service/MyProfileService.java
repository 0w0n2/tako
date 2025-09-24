package com.bukadong.tcg.api.member.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaAttachmentService;
import com.bukadong.tcg.api.media.service.MediaUrlService;
import com.bukadong.tcg.api.media.util.MediaDirResolver;
import com.bukadong.tcg.api.member.dto.request.UpdateMyProfileRequest;
import com.bukadong.tcg.api.member.dto.response.MyProfileResponse;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.api.notification.entity.NotificationSetting;
import com.bukadong.tcg.api.notification.entity.NotificationType;
import com.bukadong.tcg.api.notification.entity.NotificationTypeCode;
import com.bukadong.tcg.api.notification.repository.NotificationSettingRepository;
import com.bukadong.tcg.api.notification.repository.NotificationTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyProfileService {

    private final MemberRepository memberRepository;
    private final MediaUrlService mediaUrlService;
    private final MediaAttachmentService mediaAttachmentService;
    private final MediaDirResolver mediaDirResolver;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    @Transactional(readOnly = true)
    public MyProfileResponse loadProfile(Member me) {
        String profileUrl = mediaUrlService
                .getPrimaryImageUrl(MediaType.MEMBER_PROFILE, me.getId(), Duration.ofMinutes(5)).orElse(null);
        String backgroundUrl = mediaUrlService
                .getPrimaryImageUrl(MediaType.MEMBER_BACKGROUND, me.getId(), Duration.ofMinutes(5)).orElse(null);

        List<NotificationSetting> settings = notificationSettingRepository.findByMember(me);
        Map<String, Integer> map = settings.stream().collect(Collectors.toMap(
                s -> s.getNotificationType().getCode().name(), s -> Boolean.TRUE.equals(s.getEnabled()) ? 1 : 0));

        return MyProfileResponse.toDto(me, profileUrl, backgroundUrl, map);
    }

    @Transactional
    public void updateProfile(Member me, UpdateMyProfileRequest req, MultipartFile profileImage,
            MultipartFile backgroundImage) {
        if (req.nickname() != null) {
            me.changeNickname(req.nickname());
        }
        if (req.introduction() != null) {
            me.changeIntroduction(req.introduction());
        }
        if (req.notificationSetting() != null) {
            upsertNotificationSettings(me, req.notificationSetting());
        }

        // 이미지 교체 처리: 파일이 온 경우 기존 이미지 전체 삭제 후 새 파일 1장 업로드
        if (profileImage != null && !profileImage.isEmpty()) {
            String dir = mediaDirResolver.resolve(MediaType.MEMBER_PROFILE);
            // 본인 프로필의 기존 이미지 모두 삭제
            mediaAttachmentService.removeAll(MediaType.MEMBER_PROFILE, me.getId(), me);
            // 새 이미지 업로드 (단건 리스트화)
            mediaAttachmentService.addByMultipart(MediaType.MEMBER_PROFILE, me.getId(), me,
                    java.util.List.of(profileImage), dir);
        }

        if (backgroundImage != null && !backgroundImage.isEmpty()) {
            String dir = mediaDirResolver.resolve(MediaType.MEMBER_BACKGROUND);
            mediaAttachmentService.removeAll(MediaType.MEMBER_BACKGROUND, me.getId(), me);
            mediaAttachmentService.addByMultipart(MediaType.MEMBER_BACKGROUND, me.getId(), me,
                    java.util.List.of(backgroundImage), dir);
        }
        memberRepository.save(me);
    }

    private void upsertNotificationSettings(Member me, Map<String, Integer> incoming) {
        for (Map.Entry<String, Integer> e : incoming.entrySet()) {
            String codeStr = e.getKey();
            Integer onoff = e.getValue();
            boolean skip = (onoff == null);
            NotificationTypeCode code = null;
            if (!skip) {
                try {
                    code = NotificationTypeCode.valueOf(codeStr);
                } catch (IllegalArgumentException ex) {
                    skip = true; // 알 수 없는 코드는 무시
                }
            }
            NotificationType t = null;
            if (!skip) {
                t = notificationTypeRepository.findByCode(code).orElse(null);
                if (t == null) {
                    skip = true;
                }
            }
            if (skip) {
                continue;
            }
            final NotificationType type = t;

            NotificationSetting existing = notificationSettingRepository.findByMemberAndNotificationType(me, type)
                    .orElseGet(() -> NotificationSetting.builder().member(me).notificationType(type).enabled(false)
                            .build());
            NotificationSetting toSave = NotificationSetting.builder().id(existing.getId()).member(me)
                    .notificationType(type).enabled(onoff == 1).build();
            notificationSettingRepository.save(toSave);
        }
    }
}
