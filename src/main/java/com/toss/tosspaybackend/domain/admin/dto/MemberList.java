package com.toss.tosspaybackend.domain.admin.dto;

import com.toss.tosspaybackend.domain.member.enums.AccountStatus;
import lombok.Builder;

@Builder
public record MemberList(
        Long id,
        String phone,
        String name,
        String createdAt,
        AccountStatus accountStatus,
        String totalBalance
) {
}
