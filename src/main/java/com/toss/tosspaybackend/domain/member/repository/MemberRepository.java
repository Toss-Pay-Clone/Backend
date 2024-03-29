package com.toss.tosspaybackend.domain.member.repository;

import com.toss.tosspaybackend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByNameAndPhoneAndResidentRegistrationNumberFront(String name, String phone, String frontRRN);
    Optional<Member> findByPhone(String phone);
    @Query(value = "SELECT * FROM member m WHERE m.phone = :phone AND m.deleted_at IS NOT NULL", nativeQuery = true)
    Optional<Member> findByPhoneAndDeleted(@Param("phone") String phone);
    boolean existsByPhone(String phone);
}
