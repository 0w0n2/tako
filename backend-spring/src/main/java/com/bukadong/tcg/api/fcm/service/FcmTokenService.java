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
        // FCM 토큰 등록
        // 1. 토큰을 조회한다.
        // 2. 없으면 새로 저장.
        // 3. 있으면 같은 회원이면 아무 것도 안 함, 다른 회원이면 소유권 이전: 기존 삭제 후 새로 저장.
        var existingOpt = fcmTokenRepository.findByToken(token);
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            if (existing.getMember().getId().equals(memberId)) {
                return; // 이미 내 것 -> 종료
            }
            fcmTokenRepository.delete(existing); // 소유권 이전 위해 삭제
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));
        fcmTokenRepository.save(FcmToken.builder().member(member).token(token).build());
    }

    /**
     * 특정 토큰 해제: 해당 회원이 가진 토큰일 때만 삭제 (보안상 회원 소유 검증)
     */
    @Transactional
    public void unregister(Long memberId, String token) {
        fcmTokenRepository.findByToken(token).ifPresent(existing -> {
            if (existing.getMember().getId().equals(memberId)) {
                fcmTokenRepository.delete(existing);
            }
        });
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
