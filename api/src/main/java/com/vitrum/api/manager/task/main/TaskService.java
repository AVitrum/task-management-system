package com.vitrum.api.manager.task.main;

import com.vitrum.api.credentials.user.User;
import com.vitrum.api.dto.Request.TaskRequest;
import com.vitrum.api.manager.member.Member;
import com.vitrum.api.manager.member.MemberRepository;
import com.vitrum.api.manager.task.history.OldTask;
import com.vitrum.api.manager.task.history.OldTaskRepository;
import com.vitrum.api.manager.team.TeamRepository;
import com.vitrum.api.util.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repository;
    private final OldTaskRepository oldTaskRepository;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final Converter converter;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void create(TaskRequest request, Principal connectedUser, String teamName) {
        try {
            var member = findMember(connectedUser, teamName);
            var task = Task.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .priority(request.getPriority())
                    .creationTime(LocalDateTime.now())
                    .status(Status.PENDING)
                    .dueDate(LocalDateTime.parse(request.getDueDate(), formatter))
                    .build();
            task.setMember(member);
            repository.save(task);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Can't create");
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Title is a required field");
        }
    }

    public void change(TaskRequest request, String taskTitle, Principal connectedUser, String teamName) {
        try {
            var member = findMember(connectedUser, teamName);
            var task = repository.findByTitleAndMember(taskTitle, member)
                    .orElseThrow(() -> new IllegalArgumentException("Task not found"));
            OldTask oldTask = converter.mapTaskToOldTask(task);
            if (request.getTitle() != null) {
                task.setTitle(request.getTitle());
            }
            if (request.getDescription() != null) {
                task.setDescription(request.getDescription());
            }
            if (request.getPriority() != null) {
                task.setPriority(request.getPriority());
            }
            if (request.getDueDate() != null) {
                task.setDueDate(LocalDateTime.parse(request.getDueDate(), formatter));
            }
            if (request.getStatus() != null) {
                task.setStatus(Status.valueOf(request.getStatus().toUpperCase()));
            }
            oldTaskRepository.save(oldTask);
            repository.save(task);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Can't change");
        }
    }

    public void delete(String taskTitle, Principal connectedUser, String teamName) {
        try {
            var member = findMember(connectedUser, teamName);
            var task = repository.findByTitleAndMember(taskTitle, member)
                    .orElseThrow(() -> new IllegalArgumentException("Task not found"));
            var oldTask = converter.mapTaskToOldTask(task);
            oldTask.setStatus(Status.DELETED);
            oldTaskRepository.save(oldTask);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Can't delete");
        }
    }

    private Member findMember(Principal connectedUser, String teamName) {
        var team = teamRepository.findByName(teamName)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        return memberRepository.findByUserAndTeam(user, team)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found"));
    }
}