package com.bukadong.tcg.api.auth.service;

import com.bukadong.tcg.api.auth.dto.request.SignUpRequestDto;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignUpServiceImpl implements SignUpService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    @Override
    public Member signUp(SignUpRequestDto requestDto) {
        // 이전에 탈퇴한 회원의 재가입 시 기존 정보 제거
        memberRepository.findByEmailAndIsDeleted(requestDto.email(), true)
                .ifPresent(member -> {
                    memberRepository.delete(member);
                    memberRepository.flush();   // 영속성 컨텍스트의 변경 내용을 DB에 반영
                });

        String encodedPassword = bCryptPasswordEncoder.encode(requestDto.password());
        String memberUuid = UUID.randomUUID().toString();

        // TODO: S3 배포, 설정 구현 후 디폴트 이미지 할당 로직 추가 필요
        Member member = memberRepository.save(requestDto.toMember(memberUuid, encodedPassword));
        // TODO-SECURITY: 소셜 회원가입 로직 추가 필요

        return member;
    }
}
