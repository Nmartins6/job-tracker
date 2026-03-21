package dev.nicolas.JobTracker.application.dto.history;

import java.util.List;
import java.util.UUID;

public record ApplicationHistoryResponse(
        UUID applicationId,
        List<ApplicationHistoryEventResponse> events
) {
}
