package com.bukadong.tcg.api.auth.controller;

import com.bukadong.tcg.api.auth.dto.request.SignInRequestDto;
import com.bukadong.tcg.api.auth.service.AuthenticationService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/auth")
@Tag(name = "Auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "일반 로그인 API")
    @PostMapping("/sign-in")
    public BaseResponse<Void> signIn(@Valid @RequestBody SignInRequestDto requestDto, HttpServletResponse response) {
        authenticationService.authenticate(requestDto.email(), requestDto.password());
        return BaseResponse.onSuccess();
    }
}
