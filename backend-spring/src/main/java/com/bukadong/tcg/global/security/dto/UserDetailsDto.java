package com.bukadong.tcg.global.security.dto;

import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.entity.Role;
import lombok.experimental.Delegate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Spring Security의 {@link UserDetails} 인터페이스를 구현한 커스텀 클래스
 * TODO-SECURITY: OAUth2Info 구현체 등록
 */
public class UserDetailsDto implements CustomUserDetails {

    // Member 객체의 메서드를 직접 호출 가능
    @Delegate
    private final Member member;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsDto(Member member) {
        this.member = member;
        this.authorities = getAuthorities(member.getRole());
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Role role) {
        if (role == null) {
            return Collections.emptyList();
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.getRoleName()));    // 단일 authority
        return authorities;
    }

    /* 권한 목록 반환 */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getUuid() {
        return member.getUuid();
    }

    /* 비밀번호 반환 */
    @Override
    public String getPassword() {
        return member.getPassword();
    }

    /* 사용자 고유 식별자 반환 */
    @Override
    public String getUsername() {
        return member.getEmail();
    }

    /* 계정 만료 여부 반환 (true: 만료되지 않음) */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /* 계정 잠금 여부 반환 */
    @Override
    public boolean isAccountNonLocked() {
        return CustomUserDetails.super.isAccountNonLocked();
    }

    /* 자격 증명(비밀번호) 만료 여부 반환 (true: 만료되지 않음) */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /* 계정 활성화 여부 반환 */
    @Override
    public boolean isEnabled() {
        return true;
    }

}
