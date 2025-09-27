package com.bukadong.tcg.api.notification.config;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.bukadong.tcg.api.notification.entity.NotificationType;
import com.bukadong.tcg.api.notification.entity.NotificationTypeCode;
import com.bukadong.tcg.api.notification.repository.NotificationTypeRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 알림 타입 시드 로더
 * <P>
 * 애플리케이션 시작 시 필수 알림 타입을 upsert한다(운영에서는 마이그레이션 도구 권장).
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Component
@RequiredArgsConstructor
public class NotificationTypeSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(NotificationTypeSeeder.class);

    private final NotificationTypeRepository repo;

    @Override
    public void run(ApplicationArguments args) {
        Map<NotificationTypeCode, NotificationType> existing = repo.findAll().stream()
                .collect(Collectors.toMap(NotificationType::getCode, Function.identity(), (a, b) -> a));

        Arrays.stream(NotificationTypeCode.values()).forEach(code -> {
            if (!existing.containsKey(code)) {
                String name = switch (code) {
                    case WISH_AUCTION_STARTED -> "관심 경매 시작";
                    case WISH_AUCTION_DUE_SOON -> "관심 경매 마감 임박";
                    case WISH_AUCTION_ENDED -> "관심 경매 마감";
                    case WISH_CARD_LISTED -> "관심 카드 경매 등록";
                    case AUCTION_NEW_INQUIRY -> "내 경매 새 문의";
                    case INQUIRY_ANSWERED -> "문의 답변 등록";
                    case AUCTION_WON -> "경매 낙찰";
                    case AUCTION_CLOSED_SELLER -> "경매 종료 (판매자)";
                    case AUCTION_CANCELED -> "경매 취소";
                    case DELIVERY_STARTED -> "배송 시작";
                    case DELIVERY_STATUS_CHANGED -> "배송 상태 변경";
                    case DELIVERY_CONFIRM_REQUEST -> "구매 확정 요청";
                    case DELIVERY_CONFIRMED_SELLER -> "판매 확정 완료(구매자 확정)";
                    case BID_ACCEPTED -> "입찰 성공";
                    case BID_REJECTED -> "입찰 거절";
                    case BID_FAILED -> "입찰 처리 실패";
                    case NOTICE_NEW -> "새 공지";
                };
                repo.save(NotificationType.of(code, name));
                log.info("Seeded notification type: {}", code);
            }
        });
    }
}
