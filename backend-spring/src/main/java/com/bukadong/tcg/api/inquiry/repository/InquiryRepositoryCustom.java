package com.bukadong.tcg.api.inquiry.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bukadong.tcg.api.inquiry.dto.response.InquiryListRow;

/**
 * 문의 커스텀 조회 리포지토리
 * <P>
 * QueryDSL을 이용한 복잡한 조회를 구현한다.
 * </P>
 */
public interface InquiryRepositoryCustom {

    /**
     * 문의 커스텀 조회 리포지토리
     * <P>
     * 전체 경매에서 특정 회원이 작성한 문의만 페이징으로 조회합니다.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @PARAM pageable 페이징 정보
     * @RETURN Page<InquiryListRow>
     */
    Page<InquiryListRow> fetchMyInquiries(Long memberId, Pageable pageable);

    /**
     * <P>
     * 목록 조회 시 비밀글/권한/제목생성/마스킹 처리 포함.
     * </P>
     * 
     * @PARAM auctionId 대상 경매 ID
     * @PARAM viewerId 조회자(익명/비로그인일 경우 null)
     * @PARAM pageable 페이징
     * @RETURN Page<InquiryListRow>
     */

    Page<InquiryListRow> findListForAuction(Long auctionId, Long viewerId, Pageable pageable);
}
