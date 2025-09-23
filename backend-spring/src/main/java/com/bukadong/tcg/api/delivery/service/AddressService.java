package com.bukadong.tcg.api.delivery.service;

import com.bukadong.tcg.api.delivery.dto.request.AddressCreateRequest;
import com.bukadong.tcg.api.delivery.dto.request.AddressUpdateRequest;
import com.bukadong.tcg.api.delivery.entity.Address;
import com.bukadong.tcg.api.member.entity.Member;

import java.util.List;

public interface AddressService {
    Address create(Member member, AddressCreateRequest req);

    Address update(Member member, Long id, AddressUpdateRequest req);

    void delete(Member member, Long id);

    Address get(Member member, Long id);

    List<Address> list(Member member);

    void setDefault(Member member, Long addressId);

    Address getDefault(Member member);
}
