package com.bukadong.tcg.api.trust.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bukadong.tcg.api.trust.entity.MemberTrust;

import jakarta.persistence.LockModeType;

public interface MemberTrustRepository extends JpaRepository<MemberTrust, Long> {

    Optional<MemberTrust> findByMember_Id(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select mt from MemberTrust mt where mt.member.id = :memberId")
    Optional<MemberTrust> findForUpdate(@Param("memberId") Long memberId);
}
