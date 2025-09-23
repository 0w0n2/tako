package com.bukadong.tcg.api.delivery.service;

import com.bukadong.tcg.api.delivery.dto.request.AddressCreateRequest;
import com.bukadong.tcg.api.delivery.dto.request.AddressUpdateRequest;
import com.bukadong.tcg.api.delivery.entity.Address;
import com.bukadong.tcg.api.delivery.entity.DefaultAddress;
import com.bukadong.tcg.api.delivery.repository.AddressRepository;
import com.bukadong.tcg.api.delivery.repository.DefaultAddressRepository;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final DefaultAddressRepository defaultAddressRepository;

    @Override
    @Transactional
    public Address create(Member member, AddressCreateRequest req) {
        Address address = Address.builder().member(member).placeName(req.getPlaceName()).name(req.getName())
                .phone(req.getPhone()).baseAddress(req.getBaseAddress()).addressDetail(req.getAddressDetail())
                .zipcode(req.getZipcode()).build();
        Address saved = addressRepository.save(address);

        if (req.isSetAsDefault()) {
            upsertDefault(member, saved);
        }
        return saved;
    }

    @Override
    @Transactional
    public Address update(Member member, Long id, AddressUpdateRequest req) {
        Address address = addressRepository.findByIdAndMember(id, member)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        // 엔티티가 @Setter가 없어 빌더 재생성 또는 리플렉션 대신 변경자 메서드를 간단히 추가하는게 좋지만
        // 여기서는 새 인스턴스에 id만 유지해 저장하도록 처리
        Address updated = Address.builder().id(address.getId()).member(member).placeName(req.getPlaceName())
                .name(req.getName()).phone(req.getPhone()).baseAddress(req.getBaseAddress())
                .addressDetail(req.getAddressDetail()).zipcode(req.getZipcode()).build();
        Address saved = addressRepository.save(updated);

        if (req.isSetAsDefault()) {
            upsertDefault(member, saved);
        }
        return saved;
    }

    @Override
    @Transactional
    public void delete(Member member, Long id) {
        Address address = addressRepository.findByIdAndMember(id, member)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        // 기본 배송지였다면 매핑 제거
        defaultAddressRepository.findByMember(member).filter(da -> da.getAddress().getId().equals(address.getId()))
                .ifPresent(da -> defaultAddressRepository.deleteById(da.getId()));

        addressRepository.delete(address);
    }

    @Override
    public Address get(Member member, Long id) {
        return addressRepository.findByIdAndMember(id, member)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
    }

    @Override
    public List<Address> list(Member member) {
        return addressRepository.findByMember(member);
    }

    @Override
    @Transactional
    public void setDefault(Member member, Long addressId) {
        Address address = addressRepository.findByIdAndMember(addressId, member)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
        upsertDefault(member, address);
    }

    @Override
    public Address getDefault(Member member) {
        return defaultAddressRepository.findByMember(member).map(DefaultAddress::getAddress)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
    }

    private void upsertDefault(Member member, Address address) {
        // 기존 기본 배송지 제거 후 새로 저장(멱등)
        defaultAddressRepository.deleteByMember(member);
        DefaultAddress da = DefaultAddress.builder().member(member).address(address).build();
        defaultAddressRepository.save(da);
    }
}
