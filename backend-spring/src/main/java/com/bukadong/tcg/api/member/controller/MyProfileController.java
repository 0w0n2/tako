package com.bukadong.tcg.api.member.controller;

import com.bukadong.tcg.api.auth.service.TokenAuthService;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.service.MemberWithDrawService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;

import com.bukadong.tcg.api.member.dto.request.UpdateMyProfileRequest;
import com.bukadong.tcg.api.member.dto.response.MyProfileResponse;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.api.member.service.MyProfileService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Member")
@RequestMapping("/v1/members")
@RequiredArgsConstructor
@Validated
public class MyProfileController {

    private final MemberQueryService memberQueryService;
    private final MyProfileService myProfileService;
    private final MemberWithDrawService memberWithDrawService;
    private final TokenAuthService tokenAuthService;

    @Operation(summary = "내 프로필 조회", description = "내 프로필을 조회합니다.")
    @GetMapping("/me")
    public BaseResponse<MyProfileResponse> getMe(@AuthenticationPrincipal CustomUserDetails user) {
        var me = memberQueryService.getByUuid(user.getUuid());
        return BaseResponse.onSuccess(myProfileService.loadProfile(me));
    }

    @Operation(summary = "내 프로필 수정", description = "텍스트 필드는 null이면 변경하지 않음. 이미지 파트가 오면 기존 이미지를 모두 삭제하고 새 이미지로 교체합니다.")
    @PatchMapping(path = "/me", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<Void> updateMe(@AuthenticationPrincipal CustomUserDetails user,
                                       @Parameter(description = "JSON 본문: nickname/introduction/notificationSetting") @RequestPart("request") @Valid UpdateMyProfileRequest request,
                                       @Parameter(description = "프로필 이미지(선택)") @RequestPart(name = "profileImage", required = false) MultipartFile profileImage,
                                       @Parameter(description = "배경 이미지(선택)") @RequestPart(name = "backgroundImage", required = false) MultipartFile backgroundImage) {
        var me = memberQueryService.getByUuid(user.getUuid());
        myProfileService.updateProfile(me, request, profileImage, backgroundImage);
        return BaseResponse.onSuccess();
    }

    @Operation(summary = "회원 탈퇴", description = "회원 정보를 Soft Deleted 합니다.")
    @PostMapping("/me/withdraw")
    public BaseResponse<Void> withDraw(
            HttpServletRequest request, HttpServletResponse response,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Member member = memberQueryService.getByUuid(user.getUuid());
        memberWithDrawService.withDraw(member);
        tokenAuthService.signOut(request, response);
        return BaseResponse.onSuccess();
    }
}
