package dev.nicolas.JobTracker.application.dto.stage;

import java.time.LocalDateTime;
import java.util.UUID;

public record StageResponse(
        UUID id,
        UUID applicationId,
        String name,
        Integer orderIndex,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime deadlineAt
) {
}
