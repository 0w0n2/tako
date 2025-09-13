package com.bukadong.tcg.api.inquiry.repository;

import com.bukadong.tcg.api.inquiry.dto.response.InquiryListRow;
import com.bukadong.tcg.api.inquiry.entity.Inquiry;
import com.bukadong.tcg.api.inquiry.entity.QInquiry;
import com.bukadong.tcg.api.inquiry.entity.QInquiryAnswer;
import com.bukadong.tcg.api.inquiry.util.NicknameMasker;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 문의 커스텀 레포지토리 구현
 * <P>
 * fetch-join 대신 2-step으로 답변 여부를 메모리 매핑하여 페이징/카운트 안전화.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Repository
@RequiredArgsConstructor
public class InquiryRepositoryImpl implements InquiryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 내 문의 목록 조회
     * <P>
     * 전체 경매에서 특정 회원이 작성한 문의만 페이징으로 반환합니다. 비밀글 마스킹 없이 제목/본문 앞부분으로 타이틀을 구성합니다.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @PARAM pageable 페이징 정보
     * @RETURN Page<InquiryListRow>
     */
    @Override
    public Page<InquiryListRow> fetchMyInquiries(Long memberId, Pageable pageable) {
        QInquiry inquiry = QInquiry.inquiry;
        QInquiryAnswer answer = QInquiryAnswer.inquiryAnswer;

        // content
        List<Inquiry> pageContent = queryFactory.selectFrom(inquiry).where(inquiry.author.id.eq(memberId))
                .orderBy(inquiry.id.desc()).offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        long total = queryFactory.select(inquiry.count()).from(inquiry).where(inquiry.author.id.eq(memberId))
                .fetchOne();

        // answer map (inquiryId -> answerId)
        Map<Long, Long> answerIdByInquiryId = queryFactory.select(Projections.tuple(answer.inquiry.id, answer.id))
                .from(answer)
                .where(answer.inquiry.id.in(pageContent.stream().map(Inquiry::getId).collect(Collectors.toSet())))
                .fetch().stream().collect(Collectors.toMap(t -> t.get(0, Long.class), t -> t.get(1, Long.class)));

        List<InquiryListRow> rows = pageContent.stream().map(it -> {
            String title = (it.getTitle() != null && !it.getTitle().isBlank()) ? it.getTitle()
                    : trimAsTitle(it.getContent()); // 내 글이므로 '비밀글' 마스킹 없이 노출
            String masked = NicknameMasker.mask(it.getAuthor().getNickname());
            Long ansId = answerIdByInquiryId.get(it.getId());
            return InquiryListRow.builder().id(it.getId()).answerId(ansId).title(title).maskedNickname(masked)
                    .createdAt(it.getCreatedAt()).build();
        }).toList();

        return new PageImpl<>(rows, pageable, total);
    }

    /**
     * 경매별 문의 목록
     * <P>
     * 비밀글은 제목 '비밀글입니다.'로만 노출.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @PARAM viewerId 조회자 회원 ID (null이면 비회원)
     * @PARAM pageable 페이징 정보
     * @RETURN Page<InquiryListRow>
     */
    @Override
    public Page<InquiryListRow> findListForAuction(Long auctionId, Long viewerId, Pageable pageable) {
        QInquiry inquiry = QInquiry.inquiry;
        QInquiryAnswer answer = QInquiryAnswer.inquiryAnswer;

        // content 앞부분을 잘라 임시 제목 생성
        List<Inquiry> pageContent = queryFactory.selectFrom(inquiry).where(inquiry.auction.id.eq(auctionId))
                .orderBy(inquiry.id.desc()).offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        long total = queryFactory.select(inquiry.count()).from(inquiry).where(inquiry.auction.id.eq(auctionId))
                .fetchOne();

        // 답변 맵
        Map<Long, Long> answerIdByInquiryId = queryFactory.select(Projections.tuple(answer.inquiry.id, answer.id))
                .from(answer)
                .where(answer.inquiry.id.in(pageContent.stream().map(Inquiry::getId).collect(Collectors.toSet())))
                .fetch().stream().collect(Collectors.toMap(t -> t.get(0, Long.class), t -> t.get(1, Long.class)));

        List<InquiryListRow> rows = pageContent.stream().map(it -> {
            boolean secret = it.isSecret();
            String title = secret ? "비밀글입니다."
                    : (it.getTitle() != null && !it.getTitle().isBlank() ? it.getTitle()
                            : trimAsTitle(it.getContent()));
            String masked = NicknameMasker.mask(it.getAuthor().getNickname());
            Long ansId = answerIdByInquiryId.get(it.getId());
            return InquiryListRow.builder().id(it.getId()).answerId(ansId).title(title).maskedNickname(masked)
                    .createdAt(it.getCreatedAt()).build();
        }).toList();

        return new PageImpl<>(rows, pageable, total);
    }

    private String trimAsTitle(String content) {
        if (content == null)
            return "";
        int limit = 30;
        String s = content.replaceAll("\\s+", " ").trim();
        return s.length() > limit ? s.substring(0, limit) + "…" : s;
    }
}
