package com.bukadong.tcg.api.member.trust.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bukadong.tcg.api.auction.entity.DescriptionMatch;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.api.member.trust.entity.MemberTrust;
import com.bukadong.tcg.api.member.trust.repository.MemberTrustRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberTrustServiceImpl implements MemberTrustService {

    private final MemberTrustRepository memberTrustRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public int getScore(Long memberId) {
        return memberTrustRepository.findByMember_Id(memberId).map(MemberTrust::getScore)
                .orElse(MemberTrust.DEFAULT_SCORE);
    }

    @Override
    @Transactional
    public void updateOnReview(Long targetMemberId, DescriptionMatch descriptionMatch, int star) {
        // 점수 변화 계산
        int delta = 0;
        // DescriptionMatch 가중치
        if (descriptionMatch != null) {
            switch (descriptionMatch) {
            case EXACT -> delta += 2;
            case ALMOST -> delta += 1;
            case DIFFERENT -> delta -= 1;
            }
        }
        // 별점 가중치
        switch (star) {
        case 1 -> delta -= 2;
        case 2 -> delta -= 1;
        case 3 -> delta += 0;
        case 4 -> delta += 1;
        case 5 -> delta += 2;
        default -> {
        }
        }

        // 잠금 조회 (동시성 안전)
        MemberTrust trust = memberTrustRepository.findForUpdate(targetMemberId).orElseGet(() -> {
            Member m = memberRepository.findById(targetMemberId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_USER));
            return memberTrustRepository.save(MemberTrust.builder().member(m).score(MemberTrust.DEFAULT_SCORE).build());
        });

        trust.applyDelta(delta);
        // JPA dirty checking
    }
}
