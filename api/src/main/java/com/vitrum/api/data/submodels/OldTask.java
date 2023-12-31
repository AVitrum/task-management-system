package com.vitrum.api.data.submodels;

import com.vitrum.api.data.enums.Status;
import com.vitrum.api.data.models.Task;
import com.vitrum.api.data.models.User;
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
    private Long version;
    private LocalDateTime changeTime;
    private String message;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
