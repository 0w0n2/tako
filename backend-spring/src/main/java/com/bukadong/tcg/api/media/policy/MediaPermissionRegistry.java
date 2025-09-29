package com.bukadong.tcg.api.media.policy;

import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MediaType → Policy 매핑/조회
 */
@Component
public class MediaPermissionRegistry {

    private static final Logger log = LoggerFactory.getLogger(MediaPermissionRegistry.class);

    private final Map<MediaType, MediaPermissionPolicy> policies = new EnumMap<>(MediaType.class);
    private final List<MediaPermissionPolicy> beans;

    public MediaPermissionRegistry(List<MediaPermissionPolicy> beans) {
        this.beans = beans;
    }

    @PostConstruct
    void init() {
        beans.forEach(p -> {
            policies.put(p.supports(), p);
            log.info("[MediaPolicy] registered: {} -> {}", p.supports(), p.getClass().getSimpleName());
        });
        log.info("[MediaPolicy] total registered: {}", policies.size());
    }

    public MediaPermissionPolicy get(MediaType type) {
        MediaPermissionPolicy p = policies.get(type);
        if (p == null) {
            log.warn("[MediaPolicy] no policy for type={}", type);
            throw new BaseException(BaseResponseStatus.MEDIA_FORBIDDEN);
        }
        return p;
    }
}