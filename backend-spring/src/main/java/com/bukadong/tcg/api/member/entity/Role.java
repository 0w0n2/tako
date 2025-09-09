package com.bukadong.tcg.api.member.entity;

import lombok.Getter;

/** 회원 권한 */
@Getter
public enum Role {
    USER("ROLE_ADMIN"),
    ADMIN("ROLE_USER");

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }
}
