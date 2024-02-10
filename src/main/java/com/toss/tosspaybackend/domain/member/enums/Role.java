package com.toss.tosspaybackend.domain.member.enums;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@AllArgsConstructor
public enum Role {
    ROLE_ADMIN,
    ROLE_USER;

    public GrantedAuthority asGrantedAuthority() {
        return new SimpleGrantedAuthority(name());
    }

    public String authority() {
        return name();
    }
}
