package com.bukadong.tcg.api.media.util;

import org.springframework.stereotype.Component;

import com.bukadong.tcg.api.media.entity.MediaType;

/**
 * MediaType별 기본 S3 디렉터리 결정기
 * <P>
 * 클라이언트가 dir를 넘기지 않아도 타입 기반으로 안전한 업로드 경로를 반환합니다.
 * </P>
 * 
 * @PARAM type 미디어 타입
 * @RETURN 기본 디렉터리(prefix 포함)
 */
@Component
public class MediaDirResolver {

    private static final String BASE_PREFIX = "media";

    public String resolve(MediaType type) {
        if (type == null)
            return BASE_PREFIX;
        String suffix = switch (type) {
        case MEMBER_PROFILE -> "member/profile";
        case MEMBER_BACKGROUND -> "member/background";
        case AUCTION_ITEM -> "auction/item";
        case AUCTION_AI -> "auction/ai";
        case AUCTION_REVIEW -> "auction/review";
        case CARD -> "card";
        case CATEGORY_MAJOR -> "category/major";
        case CATEGORY_MEDIUM -> "category/medium";
        case NOTICE -> "notice";
        case NOTICE_ATTACHMENT -> "notice/attachment";
        case INQUIRY -> "inquiry";
        default -> ""; // future-safe: unknown types map to base prefix
        };
        return suffix.isEmpty() ? BASE_PREFIX : BASE_PREFIX + "/" + suffix;
    }
}
