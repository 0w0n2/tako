package com.bukadong.tcg.api.auction.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 낙찰 스냅샷 DTO
 * <P>
 * 종료 시점 기준 최고가 유효 입찰 정보를 보존한다.
 * </P>
 * 
 * @PARAM bidId 입찰 ID
 * @PARAM memberId 낙찰자 회원 ID
 * @PARAM amount 낙찰 금액
 * @PARAM createdAt 입찰 시간
 * @RETURN 없음
 */
public record WinnerSnapshot(Long bidId, Long memberId, BigDecimal amount, LocalDateTime createdAt) {
}
