package com.toss.tosspaybackend.domain.admin.controller;

import com.toss.tosspaybackend.domain.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/members/{phone}")
    public String memberDetailPage(@PathVariable("phone") Long phone, Model model) {
        model.addAttribute("members", adminService.memberList());
        return "/pages/members/member-detail.html";
    }
}
