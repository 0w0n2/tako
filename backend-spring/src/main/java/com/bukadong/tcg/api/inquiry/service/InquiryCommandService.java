package com.bukadong.tcg.api.inquiry.service;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.inquiry.dto.request.AnswerCreateRequest;
import com.bukadong.tcg.api.inquiry.dto.request.AnswerUpdateRequest;
import com.bukadong.tcg.api.inquiry.dto.request.InquiryCreateRequest;
import com.bukadong.tcg.api.inquiry.dto.request.InquiryUpdateRequest;
import com.bukadong.tcg.api.inquiry.entity.Inquiry;
import com.bukadong.tcg.api.inquiry.entity.InquiryAnswer;
import com.bukadong.tcg.api.inquiry.repository.InquiryAnswerRepository;
import com.bukadong.tcg.api.inquiry.repository.InquiryRepository;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 문의/답변 명령 서비스
 * <P>
 * 쓰기 트랜잭션. 컨트롤러가 검증한 형식 외에는 비즈니스/DB 의존 검증만 수행.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InquiryCommandService {

    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository answerRepository;
    private final EntityManager em;

    /**
     * 문의 등록
     */
    public Long createInquiry(Long auctionId, Member author, InquiryCreateRequest req) {
        Auction auction = em.find(Auction.class, auctionId);
        if (auction == null)
            throw new BaseException(BaseResponseStatus.INQUIRY_AUCTION_NOT_FOUND);

        // 판매자 자기 경매에 문의 금지
        if (auction.getMember() != null && auction.getMember().getId().equals(author.getId())) {
            throw new BaseException(BaseResponseStatus.INQUIRY_CREATE_FORBIDDEN);
        }

        Inquiry inquiry = Inquiry.builder().auction(auction).author(author)
                .title(safeTitle(req.getTitle(), req.getContent())).content(req.getContent()).secret(req.isSecret())
                .build();

        Inquiry saved = inquiryRepository.save(inquiry);

        return saved.getId();
    }

    /**
     * 문의 수정(답변 전까지만)
     */
    public void updateInquiry(Long inquiryId, Member author, InquiryUpdateRequest req) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        if (!inquiry.isAuthor(author.getId()))
            throw new BaseException(BaseResponseStatus.INQUIRY_UNAUTHORIZED);

        boolean answered = answerRepository.existsByInquiryId(inquiryId);
        if (answered)
            throw new BaseException(BaseResponseStatus.INQUIRY_ANSWER_CONFLICT); // 이미 답변됨 → 수정 불가

        inquiry.updateBeforeAnswered(safeTitle(req.getTitle(), req.getContent()), req.getContent(), req.isSecret());
    }

    /**
     * 문의 삭제(답변 전까지만)
     */
    public void deleteInquiry(Long inquiryId, Member author) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        if (!inquiry.isAuthor(author.getId()))
            throw new BaseException(BaseResponseStatus.INQUIRY_UNAUTHORIZED);

        boolean answered = answerRepository.existsByInquiryId(inquiryId);
        if (answered)
            throw new BaseException(BaseResponseStatus.INQUIRY_ANSWER_CONFLICT); // 이미 답변됨 → 삭제 불가

        inquiryRepository.delete(inquiry);
    }

    /**
     * 답변 등록(판매자만)
     */
    public Long createAnswer(Long inquiryId, Member seller, AnswerCreateRequest req) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        // 판매자 권한 검증
        if (!inquiry.getAuction().getMember().getId().equals(seller.getId())) {
            throw new BaseException(BaseResponseStatus.INQUIRY_ANSWER_UNAUTHORIZED);
        }

        if (answerRepository.existsByInquiryId(inquiryId)) {
            throw new BaseException(BaseResponseStatus.INQUIRY_ANSWER_CONFLICT); // 이미 답변 존재
        }

        InquiryAnswer answer = InquiryAnswer.builder().inquiry(inquiry).seller(seller).content(req.getContent())
                .build();

        return answerRepository.save(answer).getId();
    }

    /**
     * 답변 수정(판매자만)
     */
    public void updateAnswer(Long inquiryId, Member seller, AnswerUpdateRequest req) {
        InquiryAnswer answer = answerRepository.findByInquiryId(inquiryId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INQUIRY_NOT_FOUND));

        if (!answer.getSeller().getId().equals(seller.getId())) {
            throw new BaseException(BaseResponseStatus.INQUIRY_FORBIDDEN);
        }
        answer.updateContent(req.getContent());
    }

    /**
     * 답변 삭제(판매자만)
     */
    public void deleteAnswer(Long inquiryId, Member seller) {
        InquiryAnswer answer = answerRepository.findByInquiryId(inquiryId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INQUIRY_NOT_FOUND));

        if (!answer.getSeller().getId().equals(seller.getId())) {
            throw new BaseException(BaseResponseStatus.INQUIRY_FORBIDDEN);
        }
        answerRepository.delete(answer);
    }

    private String safeTitle(String title, String content) {
        if (title != null && !title.isBlank())
            return title;
        if (content == null)
            return null;
        String s = content.replaceAll("\\s+", " ").trim();
        return s.length() > 30 ? s.substring(0, 30) + "…" : s;
    }
}
