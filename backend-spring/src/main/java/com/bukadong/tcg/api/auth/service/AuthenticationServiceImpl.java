package com.bukadong.tcg.api.auth.service;

import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationProvider authenticationProvider;

    @Override
    public CustomUserDetails authenticate(String username, String password) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationProvider.authenticate(authenticationToken);
            return (CustomUserDetails) authentication.getPrincipal();
        } catch (UsernameNotFoundException | DisabledException e) { // 존재하지 않거나, 삭제된 회원
            throw new BaseException(BaseResponseStatus.NO_EXIST_USER);
        } catch (BadCredentialsException e) {   // 비밀번호 불일치
            throw new BaseException(BaseResponseStatus.PASSWORD_MATCH_FAILED);
        } catch (AuthenticationException e) {
            throw new BaseException(BaseResponseStatus.AUTHENTICATION_REQUIRED);
        }
    }

}
