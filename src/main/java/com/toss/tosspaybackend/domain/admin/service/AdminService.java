package com.toss.tosspaybackend.domain.admin.service;

import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminService {

    private final MemberRepository memberRepository;

    public List<Member> memberList() {
        return memberRepository.findAll();
    }
}
