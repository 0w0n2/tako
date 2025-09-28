package com.bukadong.tcg.api.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.notification.entity.NotificationSetting;
import com.bukadong.tcg.api.notification.entity.NotificationType;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    List<NotificationSetting> findByMember(Member member);

    Optional<NotificationSetting> findByMemberAndNotificationType(Member member, NotificationType type);
}
