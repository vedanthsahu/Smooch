package com.incidentcommand.backend.production;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.incidentcommand.backend.identity.Team;
import com.incidentcommand.backend.identity.TeamRepository;

@RestController
@RequestMapping("/api/services")
public class ProductionServiceController {

    private final ProductionServiceRepository serviceRepository;
    private final ProductionEnvironmentRepository productionRepository;
    private final TeamRepository teamRepository;

    public ProductionServiceController(
            ProductionServiceRepository serviceRepository,
            ProductionEnvironmentRepository productionRepository,
            TeamRepository teamRepository) {
        this.serviceRepository = serviceRepository;
        this.productionRepository = productionRepository;
        this.teamRepository = teamRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ProductionServiceDto> list(@RequestParam(required = false) Long productionId) {
        List<ProductionService> services = productionId != null
                ? serviceRepository.findByProductionId(productionId)
                : serviceRepository.findAll();
        return services.stream().map(ProductionServiceDto::from).toList();
    }

    @PostMapping
    public ResponseEntity<ProductionServiceDto> create(@Valid @RequestBody CreateServiceRequest request) {
        ProductionEnvironment production = productionRepository.findById(request.productionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "production not found"));
        Team owningTeam = teamRepository.findById(request.owningTeamId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "owning team not found"));
        ProductionService saved = serviceRepository.save(
                new ProductionService(request.name(), production, owningTeam));
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductionServiceDto.from(saved));
    }
}
