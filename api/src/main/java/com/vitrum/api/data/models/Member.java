package com.vitrum.api.data.models;

import com.vitrum.api.data.enums.RoleInTeam;
import com.vitrum.api.repositories.MemberRepository;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;
import java.util.List;

@Entity
@Table(name = "member")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isEmailsAllowed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private RoleInTeam role;

    @OneToMany(mappedBy = "author")
    private List<Comment> comments;

    @OneToMany(mappedBy = "creator")
    private List<Task> creatorTasks;

    @OneToMany(mappedBy = "performer")
    private List<Task> performerTasks;

    public boolean checkPermission() {
        return this.getRole().equals(RoleInTeam.MEMBER);
    }

    public static Member getActionPerformer(MemberRepository memberRepository, Principal connectedUser, Team team) {
        return memberRepository.findByUserAndTeam(
                User.getUserFromPrincipal(connectedUser),
                team
        ).orElseThrow(() -> new IllegalArgumentException("Member not found"));
    }

    public static void create(MemberRepository repository, User user, Team team, String role) {
        repository.save(
                Member.builder()
                        .user(user)
                        .role(RoleInTeam.valueOf(role.toUpperCase()))
                        .team(team)
                        .isEmailsAllowed(true)
                        .build()
        );
    }
}
