package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionBid;
import com.bukadong.tcg.api.auction.entity.AuctionBidStatus;
import com.bukadong.tcg.api.auction.repository.AuctionBidRepository;
import com.bukadong.tcg.api.auction.repository.AuctionLockRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 입찰 이벤트 컨슈머
 * <P>
 * Redis 큐 → DB 반영(행락)으로 최종 일관성 보장.
 * </P>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BidEventConsumer {

    private final RedisTemplate<String, String> redisTemplate;
    private final BidEventApplyService bidEventApplyService;

    @PersistenceContext
    private EntityManager em;

    /** 간단 폴링 */
    @Scheduled(fixedDelay = 200)
    public void poll() {
        for (String q : redisTemplate.keys("auction:*:bidq")) {
            String json = redisTemplate.opsForList().leftPop(q);
            if (json == null)
                continue;

            try {
                bidEventApplyService.applyEvent(json);
            } catch (Exception e) {
                log.error("Bid consume failed: {}", e.toString());
                redisTemplate.opsForList().leftPush(q + ":retry", json);
            }
        }
    }
}
