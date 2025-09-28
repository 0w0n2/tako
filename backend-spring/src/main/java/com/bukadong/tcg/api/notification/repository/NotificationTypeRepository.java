package com.bukadong.tcg.api.notification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bukadong.tcg.api.notification.entity.NotificationType;
import com.bukadong.tcg.api.notification.entity.NotificationTypeCode;

/**
 * 알림 타입 리포지터리
 * <P>
 * code 기반 조회 지원.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public interface NotificationTypeRepository extends JpaRepository<NotificationType, Long> {
    Optional<NotificationType> findByCode(NotificationTypeCode code);
}
