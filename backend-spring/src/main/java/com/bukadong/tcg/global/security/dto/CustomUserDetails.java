package com.bukadong.tcg.global.security.dto;

import org.springframework.security.core.userdetails.UserDetails;

public interface CustomUserDetails extends UserDetails {
    String getUuid();
}
