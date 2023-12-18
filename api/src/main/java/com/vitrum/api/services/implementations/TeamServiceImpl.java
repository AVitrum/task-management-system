package com.vitrum.api.services.implementations;

import com.vitrum.api.data.enums.StageType;
import com.vitrum.api.data.enums.Status;
import com.vitrum.api.data.models.Team;
import com.vitrum.api.data.request.StageDueDatesRequest;
import com.vitrum.api.data.submodels.TeamStage;
import com.vitrum.api.repositories.*;
import com.vitrum.api.data.response.*;
import com.vitrum.api.data.models.Member;
import com.vitrum.api.data.request.TeamCreationRequest;
import com.vitrum.api.data.models.User;
import com.vitrum.api.services.interfaces.TeamService;
import com.vitrum.api.util.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository repository;
    private final MemberRepository memberRepository;
    private final TeamStageRepository teamStageRepository;
    private final TaskRepository taskRepository;
    private final Converter converter;

    @Override
    public TeamCreationResponse create(TeamCreationRequest request, Principal connectedUser) {
        try {
            User user = User.getUserFromPrincipal(connectedUser);
            Team team = Team.builder()
                    .name(request.getName().replaceAll("\\s", "_"))
                    .members(new ArrayList<>())
                    .build();
            repository.save(team);

            Member.create(memberRepository, user, team, "Leader");

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

    @Override
    public void setStageDueDates(StageDueDatesRequest request, String teamName, Principal connectedUser) {
        Team team = Team.findTeamByName(repository, teamName);
        Member actionPerformer = Member.getActionPerformer(memberRepository, connectedUser, team);

        if (actionPerformer.checkPermission())
            throw new IllegalStateException("You do not have permission for this action");

        if (teamStageRepository.existsByTeamAndIsCurrent(team, true))
            throw new IllegalStateException("The stages are already planned");

        createStages(request, team);
    }

    @Override
    public List<TeamResponse> getAll() {
        List<Team> teams = repository.findAll();
        return teams.stream()
                .map(converter::mapTeamToTeamResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeamResponse> findByUser(Principal connectedUser) {
        var user = User.getUserFromPrincipal(connectedUser);
        List<Member> members = memberRepository.findAllByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Can't find"));

        return members.stream()
                .map(member -> converter.mapTeamToTeamResponse(member.getTeam()))
                .collect(Collectors.toList());
    }

    @Override
    public TeamResponse findByName(String name) {
        return converter.mapTeamToTeamResponse(Team.findTeamByName(repository, name));
    }

    @Override
    public void changeStage(String teamName) {
        Team team = Team.findTeamByName(repository, teamName);
        TeamStage current = team.getCurrentStage(teamStageRepository);

        TeamStage next = teamStageRepository.findByTeamAndNumber(team, current.getNumber() + 1).orElseThrow(
                () -> new IllegalStateException("Error"));

        current.setIsCurrent(false);
        next.setIsCurrent(true);
        teamStageRepository.saveAll(Arrays.asList(current, next));

        checkTasks(team);
    }

    private void checkTasks(Team team) {
        team.getTasks().forEach(task -> {
            if (!task.getCompleted()
                    && !task.getStatus().equals(Status.PENDING)
                    && !task.getStatus().equals(Status.OVERDUE)
            ) task.setStatus(Status.OVERDUE);
            else if (!task.getStatus().equals(Status.APPROVED)
                    && !task.getStatus().equals(Status.PENDING)
            ) task.setStatus(Status.IN_REVIEW);
            taskRepository.save(task);
        });
    }

    private void createStages(StageDueDatesRequest request, Team team) {
        TeamStage.create(teamStageRepository, team, StageType.REQUIREMENTS, request.getRequirementsDueDate(), true, 1L);
        TeamStage.create(teamStageRepository, team, StageType.PROJECTING, request.getProjectingDueDate(), false, 2L);
        TeamStage.create(teamStageRepository, team, StageType.REVIEW, request.getReviewDueDate(), false, 3L);
        TeamStage.create(teamStageRepository, team, StageType.IMPLEMENTATION, request.getImplementationDueDate(), false, 4L);
        TeamStage.create(teamStageRepository, team, StageType.REVIEW, request.getSecondReviewDueDate(), false, 5L);
    }
}
