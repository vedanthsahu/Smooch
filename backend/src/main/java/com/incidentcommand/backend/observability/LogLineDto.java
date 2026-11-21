package com.incidentcommand.backend.observability;

public record LogLineDto(Long timestampMillis, String message) {
}
