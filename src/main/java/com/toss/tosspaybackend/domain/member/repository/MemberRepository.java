package com.toss.tosspaybackend.domain.member.repository;

import com.toss.tosspaybackend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByNameAndPhoneAndResidentRegistrationNumberFront(String name, String phone, String frontRRN);
}
