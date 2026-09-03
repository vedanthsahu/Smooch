package com.incidentcommand.backend.production;

public record ProductionEnvironmentDto(Long id, String name) {
    static ProductionEnvironmentDto from(ProductionEnvironment env) {
        return new ProductionEnvironmentDto(env.getId(), env.getName());
    }
}
