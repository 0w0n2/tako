package com.bukadong.tcg.api.member.service;

import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 조회 서비스
 * <P>
 * uuid를 기반으로 회원 ID/엔티티를 조회한다.
 * </P>
 * 
 * @PARAM uuid 회원 고유 식별자
 * @RETURN Member 또는 memberId
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberRepository memberRepository;

    /**
     * uuid로 Member.id 조회
     * <P>
     * 존재하지 않으면 NOT_FOUND 예외
     * </P>
     */
    public Long getIdByUuid(String uuid) {
        return memberRepository.findIdByUuid(uuid).orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
    }

    /**
     * uuid로 Member 조회
     * <P>
     * 존재하지 않으면 NOT_FOUND 예외
     * </P>
     */
    public Member getByUuid(String uuid) {
        return memberRepository.findByUuid(uuid).orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
    }
}
