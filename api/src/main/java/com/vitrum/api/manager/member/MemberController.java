package com.vitrum.api.manager.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/teams/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService service;

    @PostMapping("/{team}")
    public ResponseEntity<?> addToTeam(
            @PathVariable String team,
            @RequestBody Map<String, String> requestBody
    ) {
        String username = requestBody.get("username");
        try {
            return ResponseEntity.ok().body(service.addToTeam(username, team));
        } catch (UsernameNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}