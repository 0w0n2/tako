package com.bukadong.tcg.api.media.policy;

import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 프로필 미디어 정책
 * <P>
 * 소유자 본인만 첨부 추가/삭제 가능.
 * </P>
 */
@Component
@RequiredArgsConstructor
public class ProfileMediaPolicy implements MediaPermissionPolicy {

    private final MemberRepository memberRepository;

    @Override
    public MediaType supports() {
        return MediaType.MEMBER_PROFILE;
    }

    @Override
    public void checkCanAdd(MediaType type, Long ownerId, Member actor) {
        var owner = memberRepository.findById(ownerId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
        // 본인만 가능
        if (!owner.getId().equals(actor.getId()))
            throw new BaseException(BaseResponseStatus.MEDIA_FORBIDDEN);
    }

    @Override
    public void checkCanDelete(MediaType type, Long ownerId, Long mediaId, Member actor) {
        checkCanAdd(type, ownerId, actor);
    }
}
