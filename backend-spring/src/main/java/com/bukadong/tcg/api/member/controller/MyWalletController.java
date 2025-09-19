package com.bukadong.tcg.api.member.controller;

import com.bukadong.tcg.api.member.dto.request.WalletLinkRequestDto;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.api.member.service.MemberWalletService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 전용 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/members/me/wallet")
public class MyWalletController {

    private final MemberWalletService memberWalletService;
    private final MemberQueryService memberQueryService;

    @Operation(summary = "신규 지갑 주소 등록", description = "지갑 주소가 없는 사용자 계정에 메타마스크 또는 EVM 서비스의 지갑 주소를 연동합니다.")
    @PostMapping()
    public BaseResponse<Void> linkWallet(
            @Valid @RequestBody WalletLinkRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        memberWalletService.linkWalletAddress(me.getId(), requestDto.walletAddress());

        return BaseResponse.onSuccess();
    }
}
