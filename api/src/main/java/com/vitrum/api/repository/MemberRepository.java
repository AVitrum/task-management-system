package com.vitrum.api.repository;

import com.vitrum.api.entity.Member;
import com.vitrum.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUser(User user);
}
