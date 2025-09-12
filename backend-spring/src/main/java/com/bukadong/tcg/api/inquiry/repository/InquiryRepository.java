package com.bukadong.tcg.api.inquiry.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bukadong.tcg.api.inquiry.entity.Inquiry;

/**
 * 문의 레포지토리
 * <P>
 * 기본 CRUD + 커스텀 QueryDSL 조합
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public interface InquiryRepository extends JpaRepository<Inquiry, Long>, InquiryRepositoryCustom {
}
