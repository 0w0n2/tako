package com.bukadong.tcg.api.fcm.service;

import com.bukadong.tcg.api.fcm.entity.FcmToken;
import com.bukadong.tcg.api.fcm.repository.FcmTokenRepository;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {
    private final FcmTokenRepository fcmTokenRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void register(Long memberId, String token) {
        fcmTokenRepository.findByToken(token).ifPresent(existing -> {
            if (!existing.getMember().getId().equals(memberId)) {
                // 다른 회원이 쓰던 토큰이면 재소유 - 재사용 위해 삭제 후 다시 저장
                fcmTokenRepository.delete(existing);
            } else {
                // 동일 회원 & 동일 토큰 이미 존재 -> 조용히 반환
                return;
            }
        });
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));
        FcmToken entity = FcmToken.builder().member(member).token(token).build();
        fcmTokenRepository.save(entity);
    }

    /**
     * 재설정: 해당 회원의 기존 모든 토큰 제거 후 새 토큰 하나만 등록. 프론트에서 "이 기기로만 푸시 받기" 기능을 제공할 때 사용.
     */
    @Transactional
    public void resetSingleDevice(Long memberId, String newToken) {
        // 1) 내가 아닌 다른 회원이 쓰던 동일 토큰은 제거 (register 로직 재활용 위해 선행 삭제)
        fcmTokenRepository.findByToken(newToken).ifPresent(existing -> {
            if (!existing.getMember().getId().equals(memberId)) {
                fcmTokenRepository.delete(existing);
            }
        });
        // 2) 내 기존 토큰 전체 삭제
        fcmTokenRepository.deleteByMember_Id(memberId);
        // 3) 새 토큰 저장
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));
        FcmToken entity = FcmToken.builder().member(member).token(newToken).build();
        fcmTokenRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public FcmStatus status(Long memberId, String currentToken) {
        var tokens = fcmTokenRepository.findByMember_Id(memberId);
        boolean any = !tokens.isEmpty();
        boolean currentRegistered = currentToken != null
                && tokens.stream().anyMatch(t -> t.getToken().equals(currentToken));
        return new FcmStatus(any, currentRegistered, tokens.size());
    }

    public record FcmStatus(boolean active, boolean currentRegistered, int tokenCount) {
    }
}
