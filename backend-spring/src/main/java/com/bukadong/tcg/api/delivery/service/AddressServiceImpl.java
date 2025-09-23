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
import org.springframework.dao.DataIntegrityViolationException;
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
        // placeName 중복 사전 검증 (null/공백은 허용)
        if (req.getPlaceName() != null && !req.getPlaceName().isBlank()) {
            boolean duplicated = addressRepository.existsByMemberAndPlaceName(member, req.getPlaceName());
            if (duplicated) {
                throw new BaseException(BaseResponseStatus.ADDRESS_PLACENAME_DUPLICATION);
            }
        }
        Address address = Address.builder().member(member).placeName(req.getPlaceName()).name(req.getName())
                .phone(req.getPhone()).baseAddress(req.getBaseAddress()).addressDetail(req.getAddressDetail())
                .zipcode(req.getZipcode()).build();
        Address saved;
        try {
            saved = addressRepository.save(address);
        } catch (DataIntegrityViolationException ex) {
            // DB unique 제약(uk_address_member_place) 위반 시 매핑
            throw new BaseException(BaseResponseStatus.ADDRESS_PLACENAME_DUPLICATION);
        }

        if (req.isSetAsDefault()) {
            upsertDefault(member, saved);
        }
        return saved;
    }

    @Override
    @Transactional
    public Address update(Member member, Long id, AddressUpdateRequest req) {
        Address address = addressRepository.findByIdAndMember(id, member)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ADDRESS_NOT_FOUND));

        // placeName 변경 시 중복 검증 (자기 자신 제외)
        String newPlaceName = req.getPlaceName();
        if (newPlaceName != null && !newPlaceName.isBlank()) {
            // 현재 placeName과 동일하면 통과, 다르면 중복 체크
            String currentPlaceName = address.getPlaceName();
            if (!newPlaceName.equals(currentPlaceName)) {
                boolean duplicated = addressRepository.existsByMemberAndPlaceName(member, newPlaceName);
                if (duplicated) {
                    throw new BaseException(BaseResponseStatus.ADDRESS_PLACENAME_DUPLICATION);
                }
            }
        }

        // 엔티티가 @Setter가 없어 빌더 재생성 또는 리플렉션 대신 변경자 메서드를 간단히 추가하는게 좋지만
        // 여기서는 새 인스턴스에 id만 유지해 저장하도록 처리
        Address updated = Address.builder().id(address.getId()).member(member).placeName(req.getPlaceName())
                .name(req.getName()).phone(req.getPhone()).baseAddress(req.getBaseAddress())
                .addressDetail(req.getAddressDetail()).zipcode(req.getZipcode()).build();
        Address saved;
        try {
            saved = addressRepository.save(updated);
        } catch (DataIntegrityViolationException ex) {
            // DB unique 제약(uk_address_member_place) 위반 시 매핑
            throw new BaseException(BaseResponseStatus.ADDRESS_PLACENAME_DUPLICATION);
        }

        if (req.isSetAsDefault()) {
            upsertDefault(member, saved);
        }
        return saved;
    }

    @Override
    @Transactional
    public void delete(Member member, Long id) {
        Address address = addressRepository.findByIdAndMember(id, member)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ADDRESS_NOT_FOUND));

        // 기본 배송지였다면 매핑 제거
        defaultAddressRepository.findByMember(member).filter(da -> da.getAddress().getId().equals(address.getId()))
                .ifPresent(da -> defaultAddressRepository.deleteById(da.getId()));

        addressRepository.delete(address);
    }

    @Override
    public Address get(Member member, Long id) {
        return addressRepository.findByIdAndMember(id, member)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ADDRESS_NOT_FOUND));
    }

    @Override
    public List<Address> list(Member member) {
        return addressRepository.findByMember(member);
    }

    @Override
    @Transactional
    public void setDefault(Member member, Long addressId) {
        Address address = addressRepository.findByIdAndMember(addressId, member)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ADDRESS_NOT_FOUND));
        upsertDefault(member, address);
    }

    @Override
    public Address getDefault(Member member) {
        return defaultAddressRepository.findByMember(member).map(DefaultAddress::getAddress)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.DEFAULT_ADDRESS_NOT_FOUND));
    }

    private void upsertDefault(Member member, Address address) {
        // 회원의 기존 기본 배송지 조회
        defaultAddressRepository.findByMember(member).ifPresentOrElse(da -> da.changeAddress(address), () -> {
            // 없으면 새로 생성
            DefaultAddress da = DefaultAddress.builder().member(member).address(address).build();
            defaultAddressRepository.save(da);
        });
    }
}
