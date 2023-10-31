package com.vitrum.api.manager.task.history;

import com.vitrum.api.manager.member.Member;
import com.vitrum.api.manager.task.main.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "old_task")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OldTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    private String description;
    private LocalDateTime creationTime;
    private LocalDateTime dueDate;
    private LocalDateTime changeTime;
    private Long priority;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
}