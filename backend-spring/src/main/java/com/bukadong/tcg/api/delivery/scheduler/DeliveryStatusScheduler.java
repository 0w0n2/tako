package com.bukadong.tcg.api.delivery.scheduler;

import com.bukadong.tcg.api.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(DeliveryStatusScheduler.class);
    private final DeliveryService deliveryService;

    // 10초 주기 실행
    @Scheduled(cron = "*/10 * * * * *")
    public void advanceStatuses() {
        try {
            deliveryService.transitionStatuses();
        } catch (Exception e) {
            log.warn("Failed to transition delivery statuses", e);
        }
    }
}
