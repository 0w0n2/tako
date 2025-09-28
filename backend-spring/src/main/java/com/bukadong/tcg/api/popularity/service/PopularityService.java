package com.bukadong.tcg.api.popularity.service;

import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.card.entity.Card;
import com.bukadong.tcg.api.card.entity.Rarity;
import com.bukadong.tcg.api.card.repository.CardRepository;
import com.bukadong.tcg.api.popularity.dto.response.PopularCardDto;
import com.bukadong.tcg.api.popularity.util.PopularityKeyUtil;
import com.bukadong.tcg.api.media.entity.Media;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.repository.MediaRepository;
import com.bukadong.tcg.api.media.service.MediaPresignQueryService;
import com.bukadong.tcg.global.common.dto.PageResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 인기 카드 서비스
 * <P>
 * 경매 이벤트를 카드 기준으로 집계하고, 최근 60분 합산 점수로 랭킹을 제공한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Service
@RequiredArgsConstructor
public class PopularityService {

    private final StringRedisTemplate stringRedisTemplate;
    private final AuctionRepository auctionRepository;
    private final CardRepository cardRepository;
    private final MediaRepository mediaRepository;
    private final MediaPresignQueryService mediaPresignQueryService;

    /** 조회 가중치 (기본 1) */
    @Value("${popularity.weight.view}")
    private double viewWeight;

    /** 입찰 가중치 (기본 5) */
    @Value("${popularity.weight.bid}")
    private double bidWeight;

    /** 분 버킷 TTL (분) */
    @Value("${popularity.bucket.ttl-minutes}")
    private long bucketTtlMinutes;

    /**
     * 조회 이벤트 기록
     * <P>
     * 경매 → 카드 매핑 후 해당 카드의 현재 분 버킷 점수를 증가시킨다.
     * </P>
     * 
     * @PARAM categoryId 카테고리 ID
     * @PARAM auctionId 경매 ID
     * @RETURN 없음
     */
    public void recordView(long categoryId, long auctionId) {
        applyEventToMinuteBucket(categoryId, auctionId, viewWeight);
    }

    /**
     * 입찰 이벤트 기록
     * <P>
     * 경매 → 카드 매핑 후 해당 카드의 현재 분 버킷 점수를 증가시킨다(입찰 가중치).
     * </P>
     * 
     * @PARAM categoryId 카테고리 ID
     * @PARAM auctionId 경매 ID
     * @RETURN 없음
     */
    public void recordBid(long categoryId, long auctionId) {
        applyEventToMinuteBucket(categoryId, auctionId, bidWeight);
    }

    /**
     * 카드 상세 조회 시 조회 이벤트 기록
     * <p>
     * auctionId가 없는 카드 상세 화면에서도 인기도를 반영하기 위해 cardId 기준으로 카운팅한다.
     * 카드의 대분류(categoryMajor) 기준 분 버킷에 해당 cardId 멤버의 점수를 증가시킨다.
     * </p>
     *
     * @param cardId 카드 ID
     */
    public void recordCardDetailView(long cardId) {
        // 카드 → 대분류 ID 조회
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            return;
        }
        Card c = cardOpt.get();
        if (c.getCategoryMajor() == null || c.getCategoryMajor().getId() == null) {
            return;
        }
        long categoryMajorId = c.getCategoryMajor().getId();

        // 분 버킷 키 계산(UTC) 후 cardId 멤버 점수 증가
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String key = PopularityKeyUtil.minuteKey(categoryMajorId, now);
        stringRedisTemplate.opsForZSet().incrementScore(key, String.valueOf(cardId), viewWeight);
        stringRedisTemplate.expire(key, Duration.ofMinutes(bucketTtlMinutes));
    }

    /**
     * 최근 1시간 인기 카드 조회
     * <P>
     * 현재 분 포함 과거 59분까지의 분 버킷을 합산하여 랭킹을 계산한다.
     * </P>
     * 
     * @PARAM categoryId 카테고리 ID
     * @PARAM page 페이지(0-base)
     * @PARAM size 페이지 크기
     * @RETURN PageResponse<PopularCardDto>
     */
    @Transactional(readOnly = true)
    public PageResponse<PopularCardDto> getTopCardsLastHour(long categoryId, int page, int size) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // 1) 최근 60분 키 생성
        List<String> minuteKeys = new ArrayList<>(60);
        for (int i = 0; i < 60; i++) {
            minuteKeys.add(PopularityKeyUtil.minuteKey(categoryId, now.minusMinutes(i)));
        }

        // 2) 존재하는 키만 추려서 합산 비용 절감
        List<String> existing = minuteKeys.stream().filter(k -> Boolean.TRUE.equals(stringRedisTemplate.hasKey(k)))
                .toList();

        if (existing.isEmpty()) {
            return new PageResponse<>(List.of(), page, size, 0, 0);
        }

        // 3) 임시 결과 키
        String destKey = PopularityKeyUtil.tempKey(categoryId, UUID.randomUUID().toString());

        try {
            // 4) ZUNIONSTORE (고수준 API) — 가중치는 이벤트 시점에 반영했으므로 SUM만 필요
            String first = existing.get(0);
            List<String> others = (existing.size() > 1) ? existing.subList(1, existing.size()) : List.of();
            stringRedisTemplate.opsForZSet().unionAndStore(first, others, destKey);

            long totalElements = Optional.ofNullable(stringRedisTemplate.opsForZSet().zCard(destKey)).orElse(0L);
            int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

            long start = (long) page * size;
            long end = start + size - 1;

            // 5) 상위 범위 조회 (점수 포함)
            Set<ZSetOperations.TypedTuple<String>> range = stringRedisTemplate.opsForZSet()
                    .reverseRangeWithScores(destKey, start, end);

            if (range == null || range.isEmpty()) {
                return new PageResponse<>(List.of(), page, size, totalElements, totalPages);
            }

            // 6) 카드 ID 리스트 추출 (멤버는 cardId 문자열)
            List<Long> cardIds = range.stream().map(ZSetOperations.TypedTuple::getValue).filter(Objects::nonNull)
                    .map(Long::valueOf).toList();

            // 7) 카드 메타 로딩 (rarity 포함)
            Map<Long, Card> cardMap = cardRepository.findAllById(cardIds).stream()
                    .collect(Collectors.toMap(Card::getId, c -> c));

            // 7-1) 카드 대표 이미지(IMAGE, seqNo=1) 벌크 조회 → ownerId(=cardId) -> s3key 매핑
            final Map<Long, String> imageKeyByCardId = cardIds.isEmpty() ? java.util.Collections.emptyMap()
                    : mediaRepository.findCardThumbnails(MediaType.CARD, cardIds).stream()
                            .collect(Collectors.toMap(Media::getOwnerId, Media::getS3keyOrUrl, (a, b) -> a)); // 중복시 첫 값
                                                                                                              // 유지

            // 8) DTO 매핑: name/rarity, 이미지는 Presigned URL로 변환
            List<PopularCardDto> content = range.stream().map(t -> {
                Long cardId = (t.getValue() != null) ? Long.valueOf(t.getValue()) : null;
                Double scoreObj = t.getScore();
                double score = (scoreObj != null) ? scoreObj.doubleValue() : 0.0;
                Card card = (cardId != null) ? cardMap.get(cardId) : null;

                String name = (card != null && card.getName() != null) ? card.getName() : null;
                String rarity = Rarity.COMMON.name();
                if (card != null && card.getRarity() != null) {
                    rarity = card.getRarity().name();
                }
                String presignedUrl = null;
                if (cardId != null) {
                    String key = imageKeyByCardId.get(cardId);
                    if (key != null) {
                        presignedUrl = mediaPresignQueryService.getPresignedUrl(key, java.time.Duration.ofMinutes(5));
                    }
                }

                return new PopularCardDto(cardId, name, rarity, score, presignedUrl);
            }).toList();

            return new PageResponse<>(content, page, size, totalElements, totalPages);

        } finally {
            // 9) 임시 키 정리
            stringRedisTemplate.delete(destKey);
        }
    }

    /**
     * 내부 공통: 분 버킷 증가
     * <P>
     * 중요 로직: 경매→카드 매핑 실패 시 조용히 무시(운영 로그만 남기고 종료).
     * </P>
     * 
     * @PARAM categoryId 카테고리 ID
     * @PARAM auctionId 경매 ID
     * @PARAM weight 가중치
     * @RETURN 없음
     */
    private void applyEventToMinuteBucket(long categoryId, long auctionId, double weight) {
        Optional<Long> cardIdOpt = auctionRepository.findCardIdByAuctionId(auctionId);
        if (cardIdOpt.isEmpty()) {
            return;
        }
        Long cardId = cardIdOpt.get();

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String key = PopularityKeyUtil.minuteKey(categoryId, now);

        // 멤버는 "cardId" 문자열 그대로 사용 → 파싱 단순화
        stringRedisTemplate.opsForZSet().incrementScore(key, cardId.toString(), weight);

        // 중요 로직: TTL로 키 폭증 방지 (최근 60분 + 버퍼)
        stringRedisTemplate.expire(key, Duration.ofMinutes(bucketTtlMinutes));
    }
}
