package com.incidentcommand.backend.production;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/productions")
public class ProductionEnvironmentController {

    private final ProductionEnvironmentRepository productionRepository;

    public ProductionEnvironmentController(ProductionEnvironmentRepository productionRepository) {
        this.productionRepository = productionRepository;
    }

    @GetMapping
    public List<ProductionEnvironmentDto> list() {
        return productionRepository.findAll().stream().map(ProductionEnvironmentDto::from).toList();
    }

    @GetMapping("/{id}")
    public ProductionEnvironmentDto get(@PathVariable Long id) {
        return productionRepository.findById(id).map(ProductionEnvironmentDto::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "production not found"));
    }

    @PostMapping
    public ResponseEntity<ProductionEnvironmentDto> create(@Valid @RequestBody CreateProductionRequest request) {
        if (productionRepository.findByName(request.name()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "production already exists");
        }
        ProductionEnvironment saved = productionRepository.save(new ProductionEnvironment(request.name()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductionEnvironmentDto.from(saved));
    }
}
