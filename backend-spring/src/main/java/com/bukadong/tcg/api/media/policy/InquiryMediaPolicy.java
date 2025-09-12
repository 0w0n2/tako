package com.bukadong.tcg.api.media.policy;

// import com.bukadong.tcg.api.inquiry.entity.Inquiry;
// import com.bukadong.tcg.api.inquiry.repository.InquiryAnswerRepository;
// import com.bukadong.tcg.api.inquiry.repository.InquiryRepository;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 문의 미디어 정책
 * <P>
 * 작성자 본인만 가능, 답변이 등록되면 불가.
 * </P>
 */
@Component
// @RequiredArgsConstructor
public class InquiryMediaPolicy implements MediaPermissionPolicy {

    // private final InquiryRepository inquiryRepository;
    // private final InquiryAnswerRepository inquiryAnswerRepository;

    @Override
    public MediaType supports() {
        return MediaType.INQUIRY;
    }

    @Override
    public void checkCanAdd(MediaType type, Long ownerId, Member actor) {
        // Inquiry inq = inquiryRepository.findById(ownerId)
        //         .orElseThrow(() -> new BaseException(BaseResponseStatus.MEDIA_NOT_FOUND));
        // // 작성자 본인만 가능
        // if (!inq.getAuthor().getId().equals(actor.getId()))
        //     throw new BaseException(BaseResponseStatus.MEDIA_FORBIDDEN);
        // // 답변이 등록된 문의는 불가
        // if (inquiryAnswerRepository.existsByInquiryId(ownerId))
        //     throw new BaseException(BaseResponseStatus.MEDIA_NOT_EDITABLE);
    }

    @Override
    public void checkCanDelete(MediaType type, Long ownerId, Long mediaId, Member actor) {
        checkCanAdd(type, ownerId, actor); // 동일 제약
    }
}
