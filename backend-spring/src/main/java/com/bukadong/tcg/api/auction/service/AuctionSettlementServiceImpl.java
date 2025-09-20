package com.bukadong.tcg.api.auction.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

/**
 * 경매 정산 서비스 기본 구현
 * <P>
 * 실서비스에선 MQ/배치로 연결하면 되고, 현재는 로그 적재로 대체한다.
 * </P>
 * 
 * @PARAM auctionId 경매 ID
 * @PARAM winnerMemberId 낙찰자 회원 ID
 * @PARAM amount 낙찰 금액
 * @RETURN 없음
 */
@Slf4j
@Service
public class AuctionSettlementServiceImpl implements AuctionSettlementService {

    @Override
    @Transactional
    public void enqueue(Long auctionId, Long winnerMemberId, BigDecimal amount) {
        // TODO: MQ/이체/정산 배치에 전달하도록 확장
        log.info("[Settlement] enqueue auctionId={}, winnerMemberId={}, amount={}", auctionId, winnerMemberId, amount);
    }
}
