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
        List<String> minuteKeys = buildMinuteKeys(categoryId, now, 60);
        List<String> existing = filterExistingKeys(minuteKeys);
        if (existing.isEmpty()) {
            return new PageResponse<>(List.of(), page, size, 0, 0);
        }

        String destKey = createTempKeyAndUnion(categoryId, existing);
        try {
            long totalElements = zCard(destKey);
            int totalPages = computeTotalPages(size, totalElements);
            long start = computeStart(page, size);
            long end = start + size - 1;

            Set<ZSetOperations.TypedTuple<String>> range = fetchRangeWithScores(destKey, start, end);
            if (range == null || range.isEmpty()) {
                return new PageResponse<>(List.of(), page, size, totalElements, totalPages);
            }

            List<Long> cardIds = extractCardIds(range);
            Map<Long, Card> cardMap = loadCards(cardIds);
            Map<Long, String> imageKeyByCardId = loadThumbnailKeys(cardIds);
            List<PopularCardDto> content = toDtoList(range, cardMap, imageKeyByCardId);

            return new PageResponse<>(content, page, size, totalElements, totalPages);
        } finally {
            stringRedisTemplate.delete(destKey);
        }
    }

    // helpers to reduce cognitive complexity
    private List<String> buildMinuteKeys(long categoryId, LocalDateTime now, int minutes) {
        List<String> keys = new ArrayList<>(minutes);
        for (int i = 0; i < minutes; i++) {
            keys.add(PopularityKeyUtil.minuteKey(categoryId, now.minusMinutes(i)));
        }
        return keys;
    }

    private List<String> filterExistingKeys(List<String> keys) {
        return keys.stream().filter(k -> Boolean.TRUE.equals(stringRedisTemplate.hasKey(k))).toList();
    }

    private String createTempKeyAndUnion(long categoryId, List<String> existing) {
        String destKey = PopularityKeyUtil.tempKey(categoryId, UUID.randomUUID().toString());
        String first = existing.get(0);
        List<String> others = (existing.size() > 1) ? existing.subList(1, existing.size()) : List.of();
        stringRedisTemplate.opsForZSet().unionAndStore(first, others, destKey);
        return destKey;
    }

    private long zCard(String key) {
        return Optional.ofNullable(stringRedisTemplate.opsForZSet().zCard(key)).orElse(0L);
    }

    private int computeTotalPages(int size, long totalElements) {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    private long computeStart(int page, int size) {
        return (long) page * size;
    }

    private Set<ZSetOperations.TypedTuple<String>> fetchRangeWithScores(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    private List<Long> extractCardIds(Set<ZSetOperations.TypedTuple<String>> range) {
        return range.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .filter(Objects::nonNull)
                .map(Long::valueOf)
                .toList();
    }

    private Map<Long, Card> loadCards(List<Long> cardIds) {
        if (cardIds.isEmpty())
            return java.util.Collections.emptyMap();
        return cardRepository.findAllById(cardIds).stream().collect(Collectors.toMap(Card::getId, c -> c));
    }

    private Map<Long, String> loadThumbnailKeys(List<Long> cardIds) {
        if (cardIds.isEmpty())
            return java.util.Collections.emptyMap();
        return mediaRepository.findCardThumbnails(MediaType.CARD, cardIds).stream()
                .collect(Collectors.toMap(Media::getOwnerId, Media::getS3keyOrUrl, (a, b) -> a));
    }

    private List<PopularCardDto> toDtoList(Set<ZSetOperations.TypedTuple<String>> range,
            Map<Long, Card> cardMap,
            Map<Long, String> imageKeyByCardId) {
        return range.stream()
                .map(t -> buildDtoFromTuple(t, cardMap, imageKeyByCardId))
                .toList();
    }

    private PopularCardDto buildDtoFromTuple(ZSetOperations.TypedTuple<String> t,
            Map<Long, Card> cardMap,
            Map<Long, String> imageKeyByCardId) {
        Long cardId = extractCardId(t);
        double score = t.getScore() != null ? t.getScore() : 0.0;
        Card card = (cardId != null) ? cardMap.get(cardId) : null;

        String name = (card != null && card.getName() != null) ? card.getName() : null;
        String rarity = (card != null && card.getRarity() != null)
                ? card.getRarity().name()
                : Rarity.COMMON.name();

        String presignedUrl = generatePresignedUrl(cardId, imageKeyByCardId);
        return new PopularCardDto(cardId, name, rarity, score, presignedUrl);
    }

    private Long extractCardId(ZSetOperations.TypedTuple<String> t) {
        return (t.getValue() != null) ? Long.valueOf(t.getValue()) : null;
    }

    private String generatePresignedUrl(Long cardId, Map<Long, String> imageKeyByCardId) {
        if (cardId == null)
            return null;
        String key = imageKeyByCardId.get(cardId);
        return (key != null) ? mediaPresignQueryService.getPresignedUrl(key, Duration.ofMinutes(5)) : null;
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
