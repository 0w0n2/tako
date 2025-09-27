package com.bukadong.tcg.api.member.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bukadong.tcg.api.member.dto.response.PublicMemberProfileResponse;
import com.bukadong.tcg.api.member.service.PublicMemberProfileService;
import com.bukadong.tcg.global.common.base.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Member")
@RequestMapping("/v1/members")
@RequiredArgsConstructor
@Validated
public class PublicMemberProfileController {

    private final PublicMemberProfileService publicMemberProfileService;

    @Operation(summary = "회원 공개 프로필 조회", description = "회원의 공개 프로필 정보를 조회합니다.")
    @GetMapping("/{userId}/public-profile")
    public BaseResponse<PublicMemberProfileResponse> getPublicProfile(
            @Parameter(description = "회원 ID", required = true) @PathVariable("userId") @Min(1) Long memberId) {
        return BaseResponse.onSuccess(publicMemberProfileService.getPublicProfile(memberId));
    }
}
