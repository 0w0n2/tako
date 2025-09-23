package com.bukadong.tcg.api.delivery.repository;

import com.bukadong.tcg.api.delivery.entity.DefaultAddress;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.delivery.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DefaultAddressRepository extends JpaRepository<DefaultAddress, Long> {
    Optional<DefaultAddress> findByMember(Member member);

    void deleteByMember(Member member);

    boolean existsByMemberAndAddress(Member member, Address address);
}
