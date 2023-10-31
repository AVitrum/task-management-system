package com.vitrum.api.task;

import com.vitrum.api.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByMember(Member member);
    Optional<Task> findByTitleAndMember(String title, Member member);
}
