package com.toss.tosspaybackend.domain.admin.controller;

import com.toss.tosspaybackend.domain.admin.service.AdminService;
import com.toss.tosspaybackend.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/admin")
@Controller
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public String adminPage() {
        return "/index";
    }

    @GetMapping("/members")
    public String membersPage(Model model) {
        model.addAttribute("members", adminService.memberList());
        return "/pages/members/members.html";
    }
}
