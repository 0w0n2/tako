package com.bukadong.tcg.api.inquiry.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bukadong.tcg.api.inquiry.dto.response.InquiryListRow;

/**
 * 문의 커스텀 레포지토리 (QueryDSL)
 * <P>
 * 목록 조회 시 비밀글/권한/제목생성/마스킹 처리 포함.
 * </P>
 * 
 * @PARAM auctionId 대상 경매 ID
 * @PARAM viewerId 조회자(익명/비로그인일 경우 null)
 * @PARAM pageable 페이징
 * @RETURN Page<InquiryListRow>
 */
public interface InquiryRepositoryCustom {
    Page<InquiryListRow> findListForAuction(Long auctionId, Long viewerId, Pageable pageable);
}
