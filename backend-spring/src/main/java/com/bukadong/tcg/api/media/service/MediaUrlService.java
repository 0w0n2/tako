package com.bukadong.tcg.api.media.service;

import com.bukadong.tcg.api.media.entity.Media;
import com.bukadong.tcg.api.media.entity.MediaKind;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.repository.MediaRepository;
import com.bukadong.tcg.global.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaUrlService {

    private final MediaRepository mediaRepository;
    private final S3Uploader s3Uploader;

    /**
     * (type, ownerId)의 지정 kind 프리사인 URL 목록 반환
     */
    @Transactional(readOnly = true)
    public List<String> getPresignedImageUrls(MediaType type, Long ownerId, MediaKind kind, Duration ttl) {
        List<Media> mediaList = mediaRepository.findByTypeAndOwnerIdOrderBySeqNoAsc(type, ownerId);
        return mediaList.stream().filter(m -> m.getMediaKind() == kind)
                .map(m -> s3Uploader.resolvePresignedUrl(m.getKey(), ttl)).toList();
    }
}
