package com.incidentcommand.backend.incident;

import java.util.List;

public record IncidentDetailDto(IncidentDto incident, List<IncidentEventDto> timeline, InvestigationDto investigation) {
}
