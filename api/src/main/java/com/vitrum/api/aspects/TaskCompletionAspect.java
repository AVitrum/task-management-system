package com.vitrum.api.aspects;

import com.vitrum.api.data.enums.StageType;
import com.vitrum.api.services.interfaces.TeamService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.time.LocalDateTime;
import java.util.*;

@Aspect
@Component
@RequiredArgsConstructor
public class TaskCompletionAspect {

    private final TeamService teamService;

    @Before("execution(* com.vitrum.api.controllers.TaskController.*(..)) && args(..)")
    public void beforeTaskModification(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                .getRequest();

        String teamName = extractTeamName(request);
        LocalDateTime deadline = teamService.findByName(teamName).getStageDueDate();
        StageType current = StageType.valueOf(teamService.findByName(teamName).getStage().toUpperCase());

        if (deadline != null && LocalDateTime.now().isAfter(deadline))
            teamService.changeStage(teamName);

        if (checkMethod(joinPoint) && current.equals(StageType.REVIEW))
            throw new IllegalStateException("Stage is over. Wait for the reviewing to end");
    }

    private boolean checkMethod(JoinPoint joinPoint) {
        return joinPoint.getSignature().getName().equals("changeStatus");
    }

    private String extractTeamName(HttpServletRequest request) {
        Object attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (attribute instanceof Map<?, ?> pathVariables)
            return (String) pathVariables.get("team");
        return null;
    }
}
