package com.vitrum.api.services.implementations;

import com.vitrum.api.data.models.*;
import com.vitrum.api.data.submodels.OldTask;
import com.vitrum.api.repositories.*;
import com.vitrum.api.data.response.HistoryResponse;
import com.vitrum.api.data.enums.Status;
import com.vitrum.api.services.interfaces.OldTaskService;
import com.vitrum.api.util.Converter;
import com.vitrum.api.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OldTaskServiceImpl implements OldTaskService {

    private final OldTaskRepository repository;
    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final BundleRepository bundleRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final MessageUtil messageUtil;
    private final Converter converter;

    @Override
    public List<HistoryResponse> findAllByTitle(
            String taskTitle,
            String teamName,
            String bundleTitle,
            Principal connectedUser
    ) {
        Bundle bundle = Bundle.findBundle(bundleRepository, Team.findTeamByName(teamRepository, teamName), bundleTitle);
        Task task = findTaskByTitleAndBundle(taskTitle, bundle);
        checkUserPermission(connectedUser, bundle);

        List<OldTask> oldTasks = getOldTasks(task);

        return oldTasks.stream()
                .map(converter::mapOldTaskToHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OldTask getByVersion(
            String taskTitle,
            String teamName,
            String bundleTitle,
            Long version,
            Principal connectedUser
    ) {
        Bundle bundle = Bundle.findBundle(bundleRepository, Team.findTeamByName(teamRepository, teamName), bundleTitle);
        Task task = findTaskByTitleAndBundle(taskTitle, bundle);
        checkUserPermission(connectedUser, bundle);

        return repository.findByTaskAndVersion(task, version)
                .orElseThrow(() -> new IllegalArgumentException("Task version not found"));
    }

    @Override
    public void restore(
            String taskTitle,
            String teamName,
            String bundleTitle,
            Long version,
            Principal connectedUser
    ) {
        OldTask oldTask = getByVersion(taskTitle, teamName, bundleTitle, version, connectedUser);
        Bundle bundle = Bundle.getBundleWithDateCheck(bundleRepository, teamRepository, taskRepository, teamName, bundleTitle);
        Task task = findTaskByTitleAndBundle(taskTitle, bundle);
        checkRestorePermission(connectedUser, bundle);

        List<OldTask> oldTasks = getOldTasks(task);
        List <OldTask> oldTaskSubList = oldTasks.subList(version.intValue(), oldTasks.size());
        oldTaskSubList.stream().map(item -> oldTask.getComments()).forEach(commentRepository::deleteAll);
        repository.deleteAll(oldTaskSubList);

        updateTaskFields(oldTask, task);

        task.setStatus(Status.RESTORED);
        taskRepository.save(task);

        bundle.saveChangeDate(bundleRepository);

        messageUtil.sendMessage(bundle.getPerformer(), "The task has been restored", task.toString());
    }

    @Override
    public void delete(String taskTitle, String teamName, String bundleTitle, Principal connectedUser) {
        Bundle bundle = Bundle.getBundleWithDateCheck(bundleRepository, teamRepository, taskRepository, teamName, bundleTitle);
        Task task = findTaskByTitleAndBundle(taskTitle, bundle);
        checkDeletePermission(connectedUser, bundle);

        List<OldTask> oldTasks = getOldTasks(task);
        oldTasks.stream().map(OldTask::getComments).forEach(commentRepository::deleteAll);
        repository.deleteAll(oldTasks);

        commentRepository.deleteAll(task.getComments());
        taskRepository.delete(task);

        bundle.saveChangeDate(bundleRepository);

        messageUtil.sendMessage(
                bundle.getPerformer(),
                task.getTitle() + " has been deleted", "The task has been deleted by "
                        + bundle.getCreator().getUser().getEmail()
        );
    }

    private void checkUserPermission(Principal connectedUser, Bundle bundle) {
        Member actionPerformer = Member.getActionPerformer(memberRepository, connectedUser, bundle.getTeam());

        if (!actionPerformer.equals(bundle.getCreator())
                && !actionPerformer.equals(bundle.getPerformer())
                && actionPerformer.checkPermission()
        ) throw new IllegalStateException("You cannot view other users' tasks");
    }

    private void checkRestorePermission(Principal connectedUser, Bundle bundle) {
        Member actionPerformer = Member.getActionPerformer(memberRepository, connectedUser, bundle.getTeam());

        if (!actionPerformer.equals(bundle.getCreator())
                && actionPerformer.checkPermission()
        ) throw new IllegalStateException("You cannot view other users' tasks");
    }

    private void checkDeletePermission(Principal connectedUser, Bundle bundle) {
        Member actionPerformer = Member.getActionPerformer(memberRepository, connectedUser, bundle.getTeam());

        if (!actionPerformer.equals(bundle.getCreator())
                && actionPerformer.checkPermission()
        ) throw new IllegalStateException("You cannot view other users' tasks");
    }

    private Task findTaskByTitleAndBundle(String taskTitle, Bundle bundle) {
        return taskRepository.findByTitleAndBundle(taskTitle, bundle)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    }

    private List<OldTask> getOldTasks(Task task) {
        return repository.findAllByTask(task)
                .orElseThrow(() -> new IllegalArgumentException("Wrong task title"));
    }

    private void updateTaskFields(OldTask oldTask, Task task) {
        task.setTitle(oldTask.getTitle());
        task.setPriority(oldTask.getPriority());
        task.setDescription(oldTask.getDescription());
        task.setVersion(oldTask.getVersion());
        task.setStatus(oldTask.getStatus());

        List<Comment> comments = new ArrayList<>();
        oldTask.getComments().forEach(comment -> {
            Comment newComment = Comment.builder()
                    .text(comment.getText())
                    .creationTime(comment.getCreationTime())
                    .author(comment.getAuthor())
                    .task(task)
                    .build();
            comments.add(newComment);
        });
        task.setComments(comments);
        commentRepository.saveAll(task.getComments());
        commentRepository.deleteAll(oldTask.getComments());
    }
}
