package com.incidentcommand.backend.incident;

/** Full lifecycle per docs/23-incident-lifecycle-and-sla.md -- only CREATED/RESOLVED are
 * actually produced by the Level 2 detection loop; the rest exist for Level 3+ (AI
 * investigation, approval, remediation). */
public enum IncidentStatus {
    DETECTED,
    CREATED,
    ACKNOWLEDGED,
    INVESTIGATING,
    DIAGNOSED,
    REMEDIATION_PROPOSED,
    APPROVED,
    REJECTED,
    REMEDIATING,
    VERIFYING,
    RESOLVED,
    ESCALATED_TO_HUMAN,
    CLOSED_SLA_BREACHED
}
