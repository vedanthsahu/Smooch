package com.incidentcommand.backend.production;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.incidentcommand.backend.identity.User;
import com.incidentcommand.backend.identity.UserRepository;

/**
 * The authorization-critical join: which user holds which role on which
 * production. See docs/15-security-and-rbac.md -- every incident-scoped
 * check is (role for THIS production, permission), not just global role.
 */
@RestController
@RequestMapping("/api/production-assignments")
public class ProductionAssignmentController {

    private final ProductionAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final ProductionEnvironmentRepository productionRepository;

    public ProductionAssignmentController(
            ProductionAssignmentRepository assignmentRepository,
            UserRepository userRepository,
            ProductionEnvironmentRepository productionRepository) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.productionRepository = productionRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ProductionAssignmentDto> list(@RequestParam(required = false) Long userId) {
        List<ProductionAssignment> assignments = userId != null
                ? assignmentRepository.findByUserId(userId)
                : assignmentRepository.findAll();
        return assignments.stream().map(ProductionAssignmentDto::from).toList();
    }

    @PostMapping
    public ResponseEntity<ProductionAssignmentDto> create(@Valid @RequestBody CreateAssignmentRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        ProductionEnvironment production = productionRepository.findById(request.productionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "production not found"));
        ProductionAssignment saved = assignmentRepository.save(
                new ProductionAssignment(user, production, request.role()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductionAssignmentDto.from(saved));
    }
}
