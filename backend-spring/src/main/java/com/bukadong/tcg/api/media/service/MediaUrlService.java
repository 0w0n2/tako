package com.bukadong.tcg.api.media.service;

import com.bukadong.tcg.api.media.entity.Media;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.repository.MediaRepository;
import com.bukadong.tcg.global.util.S3Uploader;
import com.bukadong.tcg.global.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 미디어 URL 조회/발급 서비스
 * <P>
 * DB에서 media key를 조회하고, S3 presign URL을 생성한다. 같은 key에 대해서는 Redis에 TTL로 캐싱하여 중복
 * presign 비용을 줄인다.
 * </P>
 */
@Service
@RequiredArgsConstructor
public class MediaUrlService {
    private static final String PRESIGN_CACHE_PREFIX = "MEDIA:PRESIGN:";

    private static final Logger log = LoggerFactory.getLogger(MediaUrlService.class);

    private final MediaRepository mediaRepository;
    private final S3Uploader s3Uploader;
    private final RedisUtils redisUtils;

    /**
     * (type, ownerId)의 지정 kind 프리사인 URL 목록 반환 (seq_no ASC)
     * <P>
     * 각 key에 대해 presign URL을 생성하되, 동일 key/TTL 조합은 Redis 캐시를 재사용한다.
     * </P>
     * 
     * @PARAM type 미디어 타입
     * @PARAM ownerId 소유주 ID
     * @PARAM kind 미디어 종류(IMAGE/VIDEO 등)
     * @PARAM ttl presign 유효기간 (null이면 5분)
     * @RETURN presign URL 리스트
     */
    @Transactional(readOnly = true)
    public List<String> getPresignedImageUrls(MediaType type, Long ownerId, Duration ttl) {
        List<Media> mediaList = mediaRepository.findByTypeAndOwnerIdOrderBySeqNoAsc(type, ownerId);
        Duration effectiveTtl = (ttl == null ? Duration.ofMinutes(5) : ttl);

        List<String> keys = mediaList.stream().map(Media::getS3keyOrUrl).toList();

        if (log.isDebugEnabled()) {
            log.debug("[MediaPresign] 준비: type={} ownerId={} 전체미디어={} 대상키수={} TTL(초)={}", type, ownerId,
                    mediaList.size(), keys.size(), effectiveTtl.toSeconds());
        }

        List<String> urls = keys.stream().map(k -> presignWithCache(k, effectiveTtl)).filter(StringUtils::hasText)
                .toList();

        if (log.isDebugEnabled()) {
            log.debug("[MediaPresign] └─완료: type={} ownerId={} 반환URL수={} TTL(초)={}", type, ownerId, urls.size(),
                    effectiveTtl.toSeconds());
        }
        return urls;
    }

    /**
     * S3 key(또는 URL) 단건 Presign URL 발급
     * <P>
     * 입력이 http(s) URL이면 그대로 반환하고, key이면 presignWithCache를 통해 TTL 캐시를 적용해 URL을 반환한다.
     * TTL이 null이면 기본 5분을 사용한다.
     * </P>
     * 
     * @PARAM s3keyOrUrl S3 key 또는 완전한 URL
     * @PARAM ttl presign 유효기간 (null 허용)
     * @RETURN presign URL(또는 입력이 URL이면 그대로)
     */
    @Transactional(readOnly = true)
    public String getPresignedUrl(String s3keyOrUrl, Duration ttl) {
        Duration effectiveTtl = (ttl == null ? Duration.ofMinutes(5) : ttl);
        return presignWithCache(s3keyOrUrl, effectiveTtl);
    }

    /**
     * S3 key → presign URL 변환(캐시 적용)
     * <P>
     * http(s)로 시작하면 그대로 반환(캐시 미사용). key인 경우 캐시를 먼저 확인하고, 없으면 presign 생성 후 TTL-5초(최소
     * 0)로 캐시 저장한다. TTL이 다른 요청은 서로 영향을 주지 않도록 cache key에 TTL(sec)을 포함한다.
     * </P>
     * 
     * @PARAM urlOrKey S3 key 또는 이미 완전한 URL
     * @PARAM ttl presign 유효기간
     * @RETURN presign URL(또는 입력이 URL이면 그대로)
     */
    private String presignWithCache(String urlOrKey, Duration ttl) {
        if (!StringUtils.hasText(urlOrKey)) {
            if (log.isDebugEnabled())
                log.debug("[MediaPresign] └─건너뜀: 빈 키");
            return null;
        }
        String lower = urlOrKey.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            if (log.isDebugEnabled())
                log.debug("[MediaPresign] └─URL 입력: 그대로 사용 key={}", urlOrKey);
            return urlOrKey;
        }

        long ttlSec = Math.max(0, ttl.toSeconds());
        String cacheKey = PRESIGN_CACHE_PREFIX + ttlSec + ":" + urlOrKey;

        var cached = redisUtils.getValue(cacheKey, String.class);
        if (cached.isPresent()) {
            if (log.isDebugEnabled())
                log.debug("[MediaPresign] └─HIT(캐시 적중): key={} TTL(초)={} cacheKey={}", urlOrKey, ttlSec, cacheKey);
            return cached.get();
        }
        if (log.isDebugEnabled())
            log.debug("[MediaPresign] └─MISS(캐시 미스): key={} TTL(초)={} cacheKey={}", urlOrKey, ttlSec, cacheKey);

        String presigned = s3Uploader.getPresignedGetUrl(urlOrKey, ttl);
        if (!StringUtils.hasText(presigned)) {
            log.warn("[MediaPresign] └─프리사인 실패: key={} TTL(초)={}", urlOrKey, ttlSec);
            return null;
        }

        Duration cacheTtl = ttlSec > 5 ? ttl.minusSeconds(5) : ttl;
        redisUtils.setValue(cacheKey, presigned, cacheTtl);
        if (log.isDebugEnabled()) {
            log.debug("[MediaPresign] └─SET(캐시 저장): key={} TTL(초)={} cacheTTL(초)={} cacheKey={}", urlOrKey, ttlSec,
                    cacheTtl.toSeconds(), cacheKey);
        }
        return presigned;
    }

}
