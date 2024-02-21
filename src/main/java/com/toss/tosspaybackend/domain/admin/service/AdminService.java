package com.toss.tosspaybackend.domain.admin.service;

import com.toss.tosspaybackend.domain.admin.dto.MemberList;
import com.toss.tosspaybackend.domain.bank.entity.BankAccount;
import com.toss.tosspaybackend.domain.bank.repository.BankAccountRepository;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminService {

    private final MemberRepository memberRepository;
    private final BankAccountRepository bankAccountRepository;

    public List<MemberList> memberList() {
        List<Member> members = memberRepository.findAll();
        List<MemberList> resList = members.stream().map(member -> {
            LocalDateTime dateTime = member.getBaseTime().getCreatedAt();
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDateTime = dateTime.format(format);

            List<BankAccount> memberAccount = bankAccountRepository.findAllByMember(member);
            Long totalBalance = 0L;
            for (BankAccount bankAccount : memberAccount) {
                totalBalance += bankAccount.getBalance();
            }

            NumberFormat format_ = NumberFormat.getInstance();
            String formattedNumber = format_.format(totalBalance);


            return MemberList.builder()
                    .id(member.getId())
                    .phone(member.getPhone())
                    .name(member.getName())
                    .createdAt(formattedDateTime)
                    .accountStatus(member.getAccountStatus())
                    .totalBalance(formattedNumber)
                    .build();
        }).toList();
        return resList;
    }
}
