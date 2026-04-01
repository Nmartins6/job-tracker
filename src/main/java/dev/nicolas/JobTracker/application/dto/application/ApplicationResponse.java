package dev.nicolas.JobTracker.application.dto.application;

import dev.nicolas.JobTracker.domain.application.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationResponse(
        UUID id,
        UUID userId,
        UUID jobId,
        ApplicationStatus status,
        String nextAction,
        LocalDateTime nextActionDueAt
) {
}
