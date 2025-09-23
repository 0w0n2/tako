package com.bukadong.tcg.api.delivery.repository;

import com.bukadong.tcg.api.delivery.entity.Address;
import com.bukadong.tcg.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByMember(Member member);

    @Query("select a from Address a where a.id = :id and a.member = :member")
    Optional<Address> findByIdAndMember(@Param("id") Long id, @Param("member") Member member);
}
