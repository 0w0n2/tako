package com.bukadong.tcg.api.inquiry.service;

import com.bukadong.tcg.api.inquiry.dto.response.InquiryDetailResponse;
import com.bukadong.tcg.api.inquiry.dto.response.InquiryListRow;
import com.bukadong.tcg.api.inquiry.entity.Inquiry;
import com.bukadong.tcg.api.inquiry.entity.InquiryAnswer;
import com.bukadong.tcg.api.inquiry.repository.InquiryAnswerRepository;
import com.bukadong.tcg.api.inquiry.repository.InquiryRepository;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaUrlService;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final MediaUrlService mediaViewService;
    private final Logger log = LoggerFactory.getLogger(InquiryQueryService.class);

    /**
     * 내 문의 목록 조회
     * <P>
     * 전체 경매에서 특정 회원이 작성한 문의만 페이징으로 반환합니다.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @PARAM pageable 페이징 정보
     * @RETURN Page<InquiryListRow>
     */
    public Page<InquiryListRow> getMyList(Long memberId, Pageable pageable) {
        return inquiryRepository.fetchMyInquiries(memberId, pageable);
    }

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

        // 답변(없을 수 있음)
        InquiryAnswer answer = answerRepository.findByInquiryId(inquiryId).orElse(null);

        // 비밀글 권한/숨김 여부
        boolean canView = canViewSecret(inquiry, viewerId);
        boolean hide = inquiry.isSecret() && !canView;

        // presign URL 생성 (5분 유효) - 권한 없으면 빈 배열
        List<String> imageUrls = hide ? List.of()
                : mediaViewService.getPresignedImageUrls(MediaType.INQUIRY, inquiry.getId(), Duration.ofMinutes(5));

        log.debug("canViewSecret: {}", canView);

        // 응답 생성(비밀글일 경우 null/빈배열 처리)
        return InquiryDetailResponse.of(inquiry, answer, canView, hide, imageUrls);
    }

    private boolean canViewSecret(Inquiry inquiry, Long viewerId) {
        if (!inquiry.isSecret())
            return true;
        if (viewerId == null)
            return false;
        log.debug("inquiry.authorId={}, inquiry.sellerId={}, viewerId={}", inquiry.getAuthor().getId(),
                inquiry.getAuction().getMember().getId(), viewerId);
        boolean author = inquiry.isAuthor(viewerId);
        boolean seller = inquiry.getAuction().getMember().getId().equals(viewerId);
        log.debug("canViewSecret: author={}, seller={}", author, seller);
        return author || seller;
    }

}
