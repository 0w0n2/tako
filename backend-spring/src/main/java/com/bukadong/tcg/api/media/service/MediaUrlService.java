package com.bukadong.tcg.api.media.service;

import com.bukadong.tcg.api.media.entity.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * 미디어 URL 조회/발급 서비스
 * <P>
 * DB에서 media key를 조회하고, S3 presign URL을 생성한다. 같은 key에 대해서는 Redis에 TTL로 캐싱하여 중복
 * presign 비용을 줄인다.
 * </P>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaUrlService {

    // 의존성 주입(필드) 추가
    private final MediaPresignQueryService presignQueryService;

    /**
     * (type, ownerId)의 대표 이미지 presign URL 반환
     * <P>
     * seq_no ASC 기준 1번 이미지를 대표로 간주. 없으면 empty.
     * </P>
     * 
     * @PARAM type 미디어 타입
     * @PARAM ownerId 소유주 ID
     * @PARAM ttl presign 유효기간 (null이면 5분)
     * @RETURN Optional<String> presign URL
     */
    public Optional<String> getPrimaryImageUrl(MediaType type, Long ownerId, Duration ttl) {
        List<String> urls = presignQueryService.getPresignedImageUrls(type, ownerId, ttl);
        return urls.isEmpty() ? Optional.empty() : Optional.of(urls.get(0));
    }

    /**
     * (type, ownerId) IMAGE presign URL 목록 위임
     * 
     * @PARAM type 미디어 타입
     * @PARAM ownerId 소유주 ID
     * @PARAM ttl presign 유효기간
     * @RETURN URL 리스트
     */
    public List<String> getPresignedImageUrls(MediaType type, Long ownerId, Duration ttl) {
        return presignQueryService.getPresignedImageUrls(type, ownerId, ttl);
    }

    /**
     * 단건 presign URL 발급 위임
     * 
     * @PARAM s3keyOrUrl S3 key 또는 URL
     * @PARAM ttl presign 유효기간
     * @RETURN presign URL
     */
    public String getPresignedUrl(String s3keyOrUrl, Duration ttl) {
        return presignQueryService.getPresignedUrl(s3keyOrUrl, ttl);
    }
}
