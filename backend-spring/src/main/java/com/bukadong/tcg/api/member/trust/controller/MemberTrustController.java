package com.bukadong.tcg.api.member.trust.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bukadong.tcg.api.member.trust.service.MemberTrustService;
import com.bukadong.tcg.global.common.base.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/members")
@RequiredArgsConstructor
@Tag(name = "MemberTrust", description = "회원 신뢰도 API")
public class MemberTrustController {

    private final MemberTrustService memberTrustService;

    @Operation(summary = "회원 신뢰도 조회", description = "회원의 누적 신뢰도 점수를 반환합니다.")
    @GetMapping("/{memberId}/trust")
    public BaseResponse<Integer> get(@Parameter(description = "회원 ID") @PathVariable("memberId") Long memberId) {
        return BaseResponse.onSuccess(memberTrustService.getScore(memberId));
    }
}
