package com.bukadong.tcg.api.inquiry.service;

import com.bukadong.tcg.api.inquiry.dto.response.InquiryDetailResponse;
import com.bukadong.tcg.api.inquiry.dto.response.InquiryListRow;
import com.bukadong.tcg.api.inquiry.entity.Inquiry;
import com.bukadong.tcg.api.inquiry.entity.InquiryAnswer;
import com.bukadong.tcg.api.inquiry.repository.InquiryAnswerRepository;
import com.bukadong.tcg.api.inquiry.repository.InquiryRepository;
import com.bukadong.tcg.api.media.entity.Media;
import com.bukadong.tcg.api.media.entity.MediaKind;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.repository.MediaRepository;
import com.bukadong.tcg.api.media.service.MediaUrlService;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.util.S3Uploader;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Duration;

/**
 * 문의 조회 서비스
 * <P>
 * readOnly 트랜잭션. 비밀글 권한 로직 반영.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryQueryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository answerRepository;
    private final MediaRepository mediaRepository;
    private final S3Uploader s3Uploader;
    private final MediaUrlService mediaViewService;

    /**
     * 경매별 문의 목록
     * <P>
     * 비밀글은 제목 '비밀글입니다.'로만 노출.
     * </P>
     */
    public Page<InquiryListRow> getList(Long auctionId, Long viewerId, Pageable pageable) {
        return inquiryRepository.findListForAuction(auctionId, viewerId, pageable);
    }

    /**
     * 문의 상세
     * <P>
     * 비밀글이면 작성자/판매자만 본문 및 답변 내용을 조회 가능.
     * </P>
     */
    public InquiryDetailResponse getDetail(Long inquiryId, Long viewerId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        // 답변
        InquiryAnswer ans = answerRepository.findByInquiryId(inquiryId).orElse(null);

        // presign URL 생성 (5분 유효)
        List<String> imageUrls = mediaViewService.getPresignedImageUrls(MediaType.INQUIRY, inquiry.getId(),
                MediaKind.IMAGE, Duration.ofMinutes(5));

        boolean canView = canViewSecret(inquiry, viewerId);

        return InquiryDetailResponse.builder().id(inquiry.getId())
                .title(inquiry.getTitle() != null && !inquiry.getTitle().isBlank() ? inquiry.getTitle()
                        : (inquiry.isSecret() && !canView ? "비밀글입니다." : trimAsTitle(inquiry.getContent())))
                .content(inquiry.isSecret() && !canView ? null : inquiry.getContent())
                .imageUrls(inquiry.isSecret() && !canView ? List.of() : imageUrls) // (이미지) 권한 없으면 빈 배열
                .authorNickname(inquiry.getAuthor().getNickname()).createdAt(inquiry.getCreatedAt())
                .answerId(ans == null ? null : ans.getId())
                .answerContent((ans == null || (inquiry.isSecret() && !canView)) ? null : ans.getContent())
                .answerAuthorNickname(ans == null ? null : ans.getSeller().getNickname())
                .answerCreatedAt(ans == null ? null : ans.getCreatedAt()).build();
    }

    private boolean canViewSecret(Inquiry inquiry, Long viewerId) {
        if (!inquiry.isSecret())
            return true;
        if (viewerId == null)
            return false;
        boolean author = inquiry.isAuthor(viewerId);
        boolean seller = inquiry.getAuction().getMember().getId().equals(viewerId);
        return author || seller;
    }

    private String trimAsTitle(String content) {
        if (content == null)
            return "";
        int limit = 30;
        String s = content.replaceAll("\\s+", " ").trim();
        return s.length() > limit ? s.substring(0, limit) + "…" : s;
    }

}
