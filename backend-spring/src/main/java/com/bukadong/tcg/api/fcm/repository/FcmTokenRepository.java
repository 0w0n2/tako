package com.bukadong.tcg.api.fcm.repository;

import com.bukadong.tcg.api.fcm.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    List<FcmToken> findByMember_Id(Long memberId);

    Optional<FcmToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteByMember_Id(Long memberId);
}
