package com.bukadong.tcg.api.member.service;

import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberWalletServiceImpl implements MemberWalletService {
    private final MemberRepository memberRepository;

    /**
     * 현재 로그인한 사용자의 계정에 지갑 주소를 연동
     */
    @Transactional
    @Override
    public void linkWalletAddress(Long memberId, String walletAddress) {
        // 요청된 지갑 주소가 이미 다른 계정에 사용 중이면 불가능
        if (memberRepository.existsByWalletAddressAndIsDeletedIsFalse(walletAddress)) {
            throw new BaseException(BaseResponseStatus.WALLET_ADDRESS_DUPLICATED);
        }

        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_USER));

        currentMember.linkWallet(walletAddress);
    }

}
