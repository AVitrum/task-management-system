package com.vitrum.api.controllers;

import com.vitrum.api.dto.request.TeamCreationRequest;
import com.vitrum.api.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService service;

    @PostMapping("/create")
    public ResponseEntity<?> create(
            @RequestBody TeamCreationRequest request
    ) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{team}/addMember")
    public ResponseEntity<?> addMember(
            @PathVariable String team,
            @RequestBody Map<String, String> request
    ) {
        String username = request.get("username");
        try {
            service.addToTeam(username, team);
            return ResponseEntity.ok("Member added successfully");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> showByName(
            @PathVariable String name
    ) {
        try {
            return ResponseEntity.ok(service.findByName(name));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/findByUser")
    public ResponseEntity<?> showIfInTeam(
    ) {
        try {
            return ResponseEntity.ok(service.findIfInTeam());
        } catch (IllegalArgumentException | UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> showAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
