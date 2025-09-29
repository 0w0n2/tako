package com.bukadong.tcg.api.inquiry.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bukadong.tcg.api.inquiry.entity.InquiryAnswer;

import java.util.Optional;

/**
 * 문의 답변 레포지토리
 * <P>
 * 문의당 0~1개 구조.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {

    Optional<InquiryAnswer> findByInquiryId(Long inquiryId);

    boolean existsByInquiryId(Long inquiryId);
}
