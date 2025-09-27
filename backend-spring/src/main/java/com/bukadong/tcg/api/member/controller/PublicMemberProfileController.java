package com.bukadong.tcg.api.member.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bukadong.tcg.api.member.dto.response.PublicMemberProfileResponse;
import com.bukadong.tcg.api.member.service.PublicMemberProfileService;
import com.bukadong.tcg.global.common.base.BaseResponse;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/public/members")
@RequiredArgsConstructor
@Validated
public class PublicMemberProfileController {

    private final PublicMemberProfileService publicMemberProfileService;

    @GetMapping("/{userId}")
    public BaseResponse<PublicMemberProfileResponse> getPublicProfile(
            @PathVariable("userId") @Min(1) Long memberId) {
        return BaseResponse.onSuccess(publicMemberProfileService.getPublicProfile(memberId));
    }
}
