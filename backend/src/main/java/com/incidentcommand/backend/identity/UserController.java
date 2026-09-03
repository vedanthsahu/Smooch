package com.incidentcommand.backend.identity;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;

    public UserController(UserRepository userRepository, RoleRepository roleRepository, TeamRepository teamRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.teamRepository = teamRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<UserDto> list() {
        return userRepository.findAll().stream().map(UserDto::from).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public UserDto get(@PathVariable Long id) {
        return userRepository.findById(id).map(UserDto::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "user already exists");
        }
        User saved = userRepository.save(new User(request.email(), request.displayName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.from(saved));
    }

    @PostMapping("/{id}/roles")
    @Transactional
    public UserDto assignRole(@PathVariable Long id, @Valid @RequestBody AssignRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        Role role = roleRepository.findByName(request.roleName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "role not found"));
        user.getRoles().add(role);
        return UserDto.from(userRepository.save(user));
    }

    @PostMapping("/{id}/teams/{teamId}")
    @Transactional
    public UserDto addToTeam(@PathVariable Long id, @PathVariable Long teamId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "team not found"));
        user.getTeams().add(team);
        return UserDto.from(userRepository.save(user));
    }
}
