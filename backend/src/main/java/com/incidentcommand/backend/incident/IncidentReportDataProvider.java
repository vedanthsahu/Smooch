package com.incidentcommand.backend.incident;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The transactional-read half of report export, on its own bean so
 * @Transactional actually applies -- see IncidentReportExportService, which
 * needs the DTO mapping (touches lazy associations) done inside a session,
 * but the S3 upload done outside one. Same reasoning as
 * InvestigationPersistenceService.
 */
@Service
public class IncidentReportDataProvider {

    public record Report(IncidentDto incident, List<IncidentEventDto> timeline, InvestigationDto investigation) {
    }

    private final IncidentRepository incidentRepository;
    private final IncidentEventRepository incidentEventRepository;
    private final InvestigationRepository investigationRepository;

    public IncidentReportDataProvider(
            IncidentRepository incidentRepository,
            IncidentEventRepository incidentEventRepository,
            InvestigationRepository investigationRepository) {
        this.incidentRepository = incidentRepository;
        this.incidentEventRepository = incidentEventRepository;
        this.investigationRepository = investigationRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Report> buildReport(Long incidentId) {
        return incidentRepository.findById(incidentId).map(incident -> {
            List<IncidentEventDto> timeline = incidentEventRepository
                    .findByIncidentIdOrderByCreatedAtAsc(incidentId).stream()
                    .map(IncidentEventDto::from).toList();
            InvestigationDto investigation = investigationRepository.findByIncidentId(incidentId)
                    .map(InvestigationDto::from).orElse(null);
            return new Report(IncidentDto.from(incident), timeline, investigation);
        });
    }
}
