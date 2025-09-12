package com.bukadong.tcg.api.media.policy;

import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.member.entity.Member;

/**
 * 미디어 첨부에 대한 권한/상태 검증 전략
 * <P>
 * MediaType별로 추가/삭제 허용 여부를 판단한다.
 * </P>
 * 
 * @PARAM type MediaType
 * @PARAM ownerId 소유 엔터티 ID (예: Inquiry.id, Member.id, Auction.id)
 * @PARAM actor 수행자
 * @RETURN 허용이면 true (또는 예외 던지기)
 */
public interface MediaPermissionPolicy {
    MediaType supports();

    void checkCanAdd(MediaType type, Long ownerId, Member actor);

    void checkCanDelete(MediaType type, Long ownerId, Long mediaId, Member actor);
}
