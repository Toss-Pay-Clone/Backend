package com.toss.tosspaybackend.domain.member.service;

import com.toss.tosspaybackend.domain.member.enums.Role;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    public String getRoleUser() {
        return Role.ROLE_USER.authority();
    }

    public String getRoleAdmin() {
        return Role.ROLE_ADMIN.authority();
    }
}
