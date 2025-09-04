package com.bukadong.tcg.auction.dto;

import com.bukadong.tcg.auction.entity.Auction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 경매 조회용 요약 DTO.
 *
 * <p>
 * 엔티티 {@link Auction}에서 클라이언트 응답으로 전달할 주요 정보만 추출한다.
 * </p>
 *
 * <ul>
 * <li>{@code id} - 경매 ID (PK)</li>
 * <li>{@code code} - 경매 코드 (식별용 문자열)</li>
 * <li>{@code title} - 경매 제목</li>
 * <li>{@code grade} - 카드 컨디션 등급</li>
 * <li>{@code startPrice} - 시작가</li>
 * <li>{@code startDatetime} - 경매 시작 일시</li>
 * <li>{@code endDatetime} - 경매 종료 일시</li>
 * <li>{@code cardId} - 연관된 카드 ID (없을 경우 null)</li>
 * <li>{@code ownerId} - 경매 등록자(회원) ID (없을 경우 null)</li>
 * </ul>
 *
 * @see Auction
 */
public record AuctionDto(
                Long id,
                String code,
                String title,
                String grade,
                BigDecimal startPrice,
                BigDecimal currentPrice,
                LocalDateTime startDatetime,
                LocalDateTime endDatetime,
                Long cardId,
                Long ownerId) {

        /**
         * {@link Auction} 엔티티를 기반으로 {@link AuctionDto}를 생성한다.
         *
         * <p>
         * 연관 엔티티가 존재하지 않는 경우(예: PhysicalCard, Owner)에는 null을 반환한다.
         * </p>
         *
         * @param a Auction 엔티티
         * @return 변환된 AuctionDto
         */
        public static AuctionDto of(Auction a) {
                return new AuctionDto(
                                a.getId(),
                                a.getCode(),
                                a.getTitle(),
                                a.getGrade(),
                                a.getStartPrice(),
                                a.getCurrentPrice(),
                                a.getStartDatetime(),
                                a.getEndDatetime(),
                                a.getPhysicalCard() != null && a.getPhysicalCard().getCard() != null
                                                ? a.getPhysicalCard().getCard().getId()
                                                : null,
                                a.getMember() != null ? a.getMember().getId() : null);
        }
}
