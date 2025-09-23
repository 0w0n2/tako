package com.bukadong.tcg.api.delivery.controller;

import com.bukadong.tcg.api.delivery.dto.request.AddressCreateRequest;
import com.bukadong.tcg.api.delivery.dto.request.AddressUpdateRequest;
import com.bukadong.tcg.api.delivery.dto.response.AddressDetailResponse;
import com.bukadong.tcg.api.delivery.dto.response.AddressSummaryResponse;
import com.bukadong.tcg.api.delivery.dto.response.DefaultAddressResponse;
import com.bukadong.tcg.api.delivery.entity.Address;
import com.bukadong.tcg.api.delivery.service.AddressService;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    public BaseResponse<AddressDetailResponse> create(@AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid AddressCreateRequest req) {
        var me = memberQueryService.getByUuid(user.getUuid());
        Address saved = addressService.create(me, req);
        return BaseResponse.onSuccess(toDetail(saved));
    }

    @Operation(summary = "배송지 수정")
    @PutMapping("/{addressId}")
    public BaseResponse<AddressDetailResponse> update(@AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "배송지 ID", required = true) @PathVariable("addressId") Long addressId,
            @RequestBody @Valid AddressUpdateRequest req) {
        var me = memberQueryService.getByUuid(user.getUuid());
        Address saved = addressService.update(me, addressId, req);
        return BaseResponse.onSuccess(toDetail(saved));
    }

    @Operation(summary = "배송지 삭제")
    @DeleteMapping("/{addressId}")
    public BaseResponse<Void> delete(@AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "배송지 ID", required = true) @PathVariable("addressId") Long addressId) {
        var me = memberQueryService.getByUuid(user.getUuid());
        addressService.delete(me, addressId);
        return BaseResponse.onSuccess();
    }

    @Operation(summary = "배송지 상세 조회")
    @GetMapping("/{addressId}")
    public BaseResponse<AddressDetailResponse> get(@AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "배송지 ID", required = true) @PathVariable("addressId") Long addressId) {
        var me = memberQueryService.getByUuid(user.getUuid());
        Address a = addressService.get(me, addressId);
        boolean isDefault = false;
        try {
            Address def = addressService.getDefault(me);
            isDefault = def.getId().equals(a.getId());
        } catch (Exception ignored) {
            // 기본 배송지 미설정 시 false 유지
        }
        return BaseResponse.onSuccess(AddressDetailResponse.builder().id(a.getId()).placeName(a.getPlaceName())
                .name(a.getName()).phone(a.getPhone()).baseAddress(a.getBaseAddress())
                .addressDetail(a.getAddressDetail()).zipcode(a.getZipcode()).isDefault(isDefault).build());
    }

    @Operation(summary = "배송지 목록(요약)")
    @GetMapping
    public BaseResponse<List<AddressSummaryResponse>> list(@AuthenticationPrincipal CustomUserDetails user) {
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
                .map(a -> AddressSummaryResponse.builder().id(a.getId()).placeName(a.getPlaceName())
                        .baseAddress(a.getBaseAddress()).zipcode(a.getZipcode())
                        .isDefault(finalDefaultAddress != null && finalDefaultAddress.getId().equals(a.getId()))
                        .build())
                .toList();
        return BaseResponse.onSuccess(result);
    }

    @Operation(summary = "기본 배송지 설정")
    @PostMapping("/{addressId}/default")
    public BaseResponse<Void> setDefault(@AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "배송지 ID", required = true) @PathVariable("addressId") Long addressId) {
        var me = memberQueryService.getByUuid(user.getUuid());
        addressService.setDefault(me, addressId);
        return BaseResponse.onSuccess();
    }

    @Operation(summary = "기본 배송지 조회(요약)")
    @GetMapping("/default")
    public BaseResponse<DefaultAddressResponse> getDefault(@AuthenticationPrincipal CustomUserDetails user) {
        var me = memberQueryService.getByUuid(user.getUuid());
        Address a = addressService.getDefault(me);
        return BaseResponse.onSuccess(DefaultAddressResponse.builder().id(a.getId()).placeName(a.getPlaceName())
                .baseAddress(a.getBaseAddress()).zipcode(a.getZipcode()).build());
    }

    private AddressDetailResponse toDetail(Address a) {
        return AddressDetailResponse.builder().id(a.getId()).placeName(a.getPlaceName()).name(a.getName())
                .phone(a.getPhone()).baseAddress(a.getBaseAddress()).addressDetail(a.getAddressDetail())
                .zipcode(a.getZipcode()).isDefault(false) // 목록/조회에서 별도 계산 생성시 초기화시 false
                .build();
    }
}
