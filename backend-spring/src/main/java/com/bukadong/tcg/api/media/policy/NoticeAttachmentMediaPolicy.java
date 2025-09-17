package com.bukadong.tcg.api.media.policy;

import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class NoticeAttachmentMediaPolicy implements MediaPermissionPolicy {
    @Override
    public MediaType supports() {
        return MediaType.NOTICE_ATTACHMENT;
    }

    @Override
    public void checkCanAdd(MediaType type, Long ownerId, Member actor) {

    }

    @Override
    public void checkCanDelete(MediaType type, Long ownerId, Long mediaId, Member actor) {

    }
}
