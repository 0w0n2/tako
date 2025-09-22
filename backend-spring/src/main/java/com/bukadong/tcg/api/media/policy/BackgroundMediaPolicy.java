package com.bukadong.tcg.api.media.policy;

import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 회원 배경 이미지 미디어 정책 본인만 첨부 추가/삭제 가능.
 */
@Component
@RequiredArgsConstructor
public class BackgroundMediaPolicy implements MediaPermissionPolicy {

    private final MemberRepository memberRepository;

    @Override
    public MediaType supports() {
        return MediaType.MEMBER_BACKGROUND;
    }

    @Override
    public void checkCanAdd(MediaType type, Long ownerId, Member actor) {
        var owner = memberRepository.findById(ownerId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
        if (!owner.getId().equals(actor.getId())) {
            throw new BaseException(BaseResponseStatus.MEDIA_FORBIDDEN);
        }
    }

    @Override
    public void checkCanDelete(MediaType type, Long ownerId, Long mediaId, Member actor) {
        checkCanAdd(type, ownerId, actor);
    }
}
