package com.incidentcommand.backend.production;

public record ProductionServiceDto(
        Long id,
        String name,
        Long productionId,
        String productionName,
        Long owningTeamId,
        String owningTeamName) {

    static ProductionServiceDto from(ProductionService service) {
        return new ProductionServiceDto(
                service.getId(),
                service.getName(),
                service.getProduction().getId(),
                service.getProduction().getName(),
                service.getOwningTeam().getId(),
                service.getOwningTeam().getName());
    }
}
