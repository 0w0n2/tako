package com.bukadong.tcg.api.notification.service;

import com.bukadong.tcg.api.notification.entity.NotificationType;
import com.bukadong.tcg.api.notification.entity.NotificationTypeCode;
import com.bukadong.tcg.api.notification.repository.NotificationRepository;
import com.bukadong.tcg.api.notification.repository.NotificationTypeRepository;
import com.bukadong.tcg.api.wish.repository.WishQueryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 경매 알림 스케줄러
 * <P>
 * - 위시 경매 시작/마감임박/마감 시 위시 유저에게 알림 발송 - 중복 방지를 위해
 * NotificationRepository.existsBy... 검사 - 운영에서는 배치 윈도우/락/오프셋 관리 권장
 * </P>
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "auction.notify", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuctionNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(AuctionNotificationScheduler.class);

    private final JdbcTemplate jdbcTemplate; // 경매 테이블 직접 조회용 (간단 쿼리)
    private final WishQueryPort wishQueryPort;
    private final NotificationCommandService notificationCommandService;
    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;

    private static final Duration WINDOW = Duration.ofMinutes(1); // 폴링 윈도우
    private static final Duration DUE_SOON_BEFORE = Duration.ofMinutes(30); // 마감 임박 기준(30분 전)

    // ====== 매 분 실행 (운영 환경에 맞춰 조정) ======
    @Scheduled(cron = "0 * * * * *")
    public void run() {
        try {
            notifyStarted();
            notifyDueSoon();
            notifyEnded();
        } catch (Exception e) {
            log.error("AuctionNotificationScheduler failed", e);
        }
    }

    /**
     * 시작된 경매: start_datetime ∈ (now - WINDOW, now]
     */
    private void notifyStarted() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime from = now.minus(WINDOW);
        String sql = """
                    SELECT id FROM auction
                     WHERE start_datetime > ? AND start_datetime <= ?
                """;
        List<Long> auctionIds = jdbcTemplate.query(sql, (rs, i) -> rs.getLong(1), from, now);

        NotificationType type = getType(NotificationTypeCode.WISH_AUCTION_STARTED);
        for (Long auctionId : auctionIds) {
            List<Long> memberIds = wishQueryPort.findMemberIdsWhoWishedAuction(auctionId);
            for (Long mid : memberIds) {
                if (notificationRepository.existsByMemberIdAndTypeAndCauseId(mid, type, auctionId))
                    continue;
                notificationCommandService.notifyWishAuctionStarted(mid, auctionId, java.util.Map.of());
            }
        }
    }

    /**
     * 마감 임박: end_datetime - DUE_SOON_BEFORE ∈ (now - WINDOW, now]
     */
    private void notifyDueSoon() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime from = now.minus(WINDOW);
        String sql = """
                    SELECT id FROM auction
                     WHERE DATE_SUB(end_datetime, INTERVAL ? MINUTE) > ?
                       AND DATE_SUB(end_datetime, INTERVAL ? MINUTE) <= ?
                """;
        int minutes = (int) DUE_SOON_BEFORE.toMinutes();
        List<Long> auctionIds = jdbcTemplate.query(sql, (rs, i) -> rs.getLong(1), minutes, from, minutes, now);

        NotificationType type = getType(NotificationTypeCode.WISH_AUCTION_DUE_SOON);
        for (Long auctionId : auctionIds) {
            List<Long> memberIds = wishQueryPort.findMemberIdsWhoWishedAuction(auctionId);
            for (Long mid : memberIds) {
                if (notificationRepository.existsByMemberIdAndTypeAndCauseId(mid, type, auctionId))
                    continue;
                notificationCommandService.notifyWishAuctionDueSoon(mid, auctionId, java.util.Map.of());
            }
        }
    }

    /**
     * 마감됨: end_datetime ∈ (now - WINDOW, now]
     */
    private void notifyEnded() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime from = now.minus(WINDOW);
        String sql = """
                    SELECT id FROM auction
                     WHERE end_datetime > ? AND end_datetime <= ?
                """;
        List<Long> auctionIds = jdbcTemplate.query(sql, (rs, i) -> rs.getLong(1), from, now);

        NotificationType type = getType(NotificationTypeCode.WISH_AUCTION_ENDED);
        for (Long auctionId : auctionIds) {
            List<Long> memberIds = wishQueryPort.findMemberIdsWhoWishedAuction(auctionId);
            for (Long mid : memberIds) {
                if (notificationRepository.existsByMemberIdAndTypeAndCauseId(mid, type, auctionId))
                    continue;
                notificationCommandService.notifyWishAuctionEnded(mid, auctionId, java.util.Map.of());
            }
        }
    }

    private NotificationType getType(NotificationTypeCode code) {
        return notificationTypeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("NotificationType missing: " + code));
    }
}
