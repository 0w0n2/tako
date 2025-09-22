package com.bukadong.tcg.api.delivery.controller;

import com.bukadong.tcg.api.delivery.dto.AddressDtos;
import com.bukadong.tcg.api.delivery.entity.Address;
import com.bukadong.tcg.api.delivery.service.AddressService;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Address")
@RequestMapping("/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final MemberQueryService memberQueryService;

    @Operation(summary = "배송지 생성")
    @PostMapping
    public BaseResponse<AddressDtos.DetailResponse> create(@AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid AddressDtos.CreateRequest req) {
        var me = memberQueryService.getByUuid(user.getUuid());
        Address saved = addressService.create(me, req);
        return BaseResponse.onSuccess(toDetail(saved));
    }

    @Operation(summary = "배송지 수정")
    @PutMapping("/{id}")
    public BaseResponse<AddressDtos.DetailResponse> update(@AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long id, @RequestBody @Valid AddressDtos.UpdateRequest req) {
        var me = memberQueryService.getByUuid(user.getUuid());
        Address saved = addressService.update(me, id, req);
        return BaseResponse.onSuccess(toDetail(saved));
    }

    @Operation(summary = "배송지 삭제")
    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long id) {
        var me = memberQueryService.getByUuid(user.getUuid());
        addressService.delete(me, id);
        return BaseResponse.onSuccess();
    }

    @Operation(summary = "배송지 상세 조회")
    @GetMapping("/{id}")
    public BaseResponse<AddressDtos.DetailResponse> get(@AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long id) {
        var me = memberQueryService.getByUuid(user.getUuid());
        Address a = addressService.get(me, id);
        boolean isDefault = false;
        try {
            Address def = addressService.getDefault(me);
            isDefault = def.getId().equals(a.getId());
        } catch (Exception ignored) {
            // 기본 배송지 미설정 시 false 유지
        }
        return BaseResponse.onSuccess(AddressDtos.DetailResponse.builder().id(a.getId()).placeName(a.getPlaceName())
                .name(a.getName()).phone(a.getPhone()).baseAddress(a.getBaseAddress())
                .addressDetail(a.getAddressDetail()).zipcode(a.getZipcode()).isDefault(isDefault).build());
    }

    @Operation(summary = "배송지 목록(요약)")
    @GetMapping
    public BaseResponse<List<AddressDtos.SummaryResponse>> list(@AuthenticationPrincipal CustomUserDetails user) {
        var me = memberQueryService.getByUuid(user.getUuid());
        List<Address> list = addressService.list(me);
        Address defaultAddress = null;
        try {
            defaultAddress = addressService.getDefault(me);
        } catch (Exception ignored) {
            // 기본 배송지가 설정되지 않은 경우: 목록에서 isDefault=false로만 표시하면 되므로 무시
        }
        Address finalDefaultAddress = defaultAddress;
        var result = list.stream()
                .map(a -> AddressDtos.SummaryResponse.builder().id(a.getId()).placeName(a.getPlaceName())
                        .baseAddress(a.getBaseAddress()).zipcode(a.getZipcode())
                        .isDefault(finalDefaultAddress != null && finalDefaultAddress.getId().equals(a.getId()))
                        .build())
                .toList();
        return BaseResponse.onSuccess(result);
    }

    @Operation(summary = "기본 배송지 설정")
    @PostMapping("/{id}/default")
    public BaseResponse<Void> setDefault(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long id) {
        var me = memberQueryService.getByUuid(user.getUuid());
        addressService.setDefault(me, id);
        return BaseResponse.onSuccess();
    }

    @Operation(summary = "기본 배송지 조회(요약)")
    @GetMapping("/default")
    public BaseResponse<AddressDtos.DefaultAddressResponse> getDefault(
            @AuthenticationPrincipal CustomUserDetails user) {
        var me = memberQueryService.getByUuid(user.getUuid());
        Address a = addressService.getDefault(me);
        return BaseResponse.onSuccess(AddressDtos.DefaultAddressResponse.builder().id(a.getId())
                .placeName(a.getPlaceName()).baseAddress(a.getBaseAddress()).zipcode(a.getZipcode()).build());
    }

    private AddressDtos.DetailResponse toDetail(Address a) {
        return AddressDtos.DetailResponse.builder().id(a.getId()).placeName(a.getPlaceName()).name(a.getName())
                .phone(a.getPhone()).baseAddress(a.getBaseAddress()).addressDetail(a.getAddressDetail())
                .zipcode(a.getZipcode()).isDefault(false) // 목록/조회에서 별도 계산
                .build();
    }
}
