package com.vitrum.api.manager.team;

import com.vitrum.api.credentials.user.UserRepository;
import com.vitrum.api.dto.Response.*;
import com.vitrum.api.manager.member.Member;
import com.vitrum.api.manager.member.MemberRepository;
import com.vitrum.api.dto.Request.TeamCreationRequest;
import com.vitrum.api.credentials.user.User;
import com.vitrum.api.manager.member.RoleInTeam;
import com.vitrum.api.util.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository repository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final Converter converter;

    public TeamCreationResponse create(TeamCreationRequest request, Principal connectedUser) {
        try {
            var user = User.getUserFromPrincipal(connectedUser);
            var team = Team.builder()
                    .name(request.getName())
                    .members(new ArrayList<>())
                    .build();
            repository.save(team);
            var member = Member.builder()
                    .user(user)
                    .role(RoleInTeam.LEADER)
                    .team(team)
                    .build();
            memberRepository.save(member);
            repository.save(team);

            return TeamCreationResponse.builder()
                    .id(team.getId())
                    .name(team.getName())
                    .creator(converter.mapUserToUserProfileResponse(user))
                    .build();

        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Can't create");
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Team with the same name already exists");
        }
    }

    public String addToTeam(String username, String teamName) {
        var team = repository.findByName(teamName)
                .orElseThrow(() -> new IllegalArgumentException("Can't find team by this name"));
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Can't find user"));

        if (inTeam(user, team)) {
            return "The user is already in the team";
        }

        var member = Member.builder()
                .user(user)
                .role(RoleInTeam.MEMBER)
                .team(team)
                .build();
        memberRepository.save(member);
        repository.save(team);

        return "The user has been added to the team";
    }

    public List<TeamResponse> getAll() {
        var teams = repository.findAll();
        return teams.stream().map(converter::mapTeamToTeamResponse).collect(Collectors.toList());
    }

    public TeamResponse findByName(String name) {
        var team = repository.findByName(name).orElseThrow(() -> new IllegalArgumentException("Team not found"));
        return converter.mapTeamToTeamResponse(team);
    }

    private Boolean inTeam(User user, Team team) {
        return team.getMembers().contains(memberRepository.findByUser(user)
                .orElse(null));
    }
}
