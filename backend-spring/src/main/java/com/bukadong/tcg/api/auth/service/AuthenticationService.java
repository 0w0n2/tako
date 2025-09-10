package com.bukadong.tcg.api.auth.service;

import com.bukadong.tcg.global.security.dto.CustomUserDetails;

public interface AuthenticationService {

    public CustomUserDetails authenticate(String username, String password);

}
