package dev.nicolas.JobTracker.application.dto.note;

import java.time.LocalDateTime;
import java.util.UUID;

public record NoteResponse(
        UUID id,
        UUID applicationId,
        UUID stageId,
        String content,
        LocalDateTime createdAt
) {
}
