package com.bukadong.tcg.api.notification.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.bukadong.tcg.api.notification.entity.Notification;
import com.bukadong.tcg.api.notification.entity.NotificationType;

/**
 * 알림 리포지터리
 * <P>
 * 단순 CRUD.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * 동일 멤버/타입/원인ID의 알림 존재 여부
     * <P>
     * 스케줄러/대량 발송 시 중복 저장 방지에 사용.
     * </P>
     * 
     * @PARAM memberId 수신자
     * @PARAM type 알림 타입 엔티티
     * @PARAM causeId 원인 리소스 ID
     * @RETURN 존재 여부
     */
    boolean existsByMemberIdAndTypeAndCauseId(Long memberId, NotificationType type, Long causeId);

    /**
     * 내 알림 페이지 조회 (최신순)
     * <P>
     * 정렬 키는 id DESC로 고정.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @PARAM pageable 페이지 정보
     * @RETURN Page<Notification>
     */
    Page<Notification> findByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);

    /**
     * 본인 소유 알림 단건 조회
     */
    Optional<Notification> findByIdAndMemberId(Long id, Long memberId);

    /**
     * 내 모든 미읽음 알림 일괄 읽음 처리
     * <P>
     * 벌크 업데이트. 변경 건수 반환.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @PARAM when 읽은 시각
     * @RETURN 변경 행 수
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Notification n set n.read = true, n.readAt = :when where n.memberId = :memberId and n.read = false")
    int markAllReadByMemberId(@Param("memberId") Long memberId, @Param("when") LocalDateTime when);

    /**
     * 미읽음 카운트
     */
    long countByMemberIdAndReadIsFalse(Long memberId);
}
