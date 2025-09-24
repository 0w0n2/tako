package com.bukadong.tcg.api.auth.service;

import com.bukadong.tcg.api.auth.dto.request.SignUpRequestDto;
import com.bukadong.tcg.api.media.entity.Media;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.repository.MediaRepository;
import com.bukadong.tcg.api.media.util.MediaDirResolver;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignUpServiceImpl implements SignUpService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Random random = new Random();

    private final int DEFAULT_PROFILE_IMAGE_COUNT = 9;
    private final int DEFAULT_BACKGROUND_IMAGE_COUNT = 1;
    private final MediaDirResolver mediaDirResolver;
    private final MediaRepository mediaRepository;

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

        Member member = memberRepository.save(requestDto.toMember(memberUuid, encodedPassword));

        // 디폴트 프로필, 배경화면 이미지 설정
        settingDefaultMemberImage(member, MediaType.MEMBER_PROFILE, DEFAULT_PROFILE_IMAGE_COUNT);
        settingDefaultMemberImage(member, MediaType.MEMBER_BACKGROUND, DEFAULT_BACKGROUND_IMAGE_COUNT);

        // TODO-SECURITY: 소셜 회원가입 로직 추가 필요
        return member;
    }

    private void settingDefaultMemberImage(Member member, MediaType mediaType, int defaultImageCount) {
        String defaultImageUrl = "%s/default/%d.png".formatted(mediaDirResolver.resolve(mediaType), random.nextInt(defaultImageCount) + 1);
        Media m = Media.builder()
                .type(mediaType)
                .ownerId(member.getId())
                .s3keyOrUrl(defaultImageUrl)
                .mimeType("image/png")
                .seqNo(1)
                .build();
        mediaRepository.save(m);
    }
}
