package dev.nicolas.JobTracker.application.dto.history;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationHistoryEventResponse(
        ApplicationHistoryEventType type,
        UUID referenceId,
        UUID stageId,
        String title,
        String description,
        LocalDateTime occurredAt
) {
}
