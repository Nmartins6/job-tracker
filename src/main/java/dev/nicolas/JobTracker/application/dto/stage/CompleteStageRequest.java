package dev.nicolas.JobTracker.application.dto.stage;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CompleteStageRequest(
        @NotNull(message = "Data de conclusão da etapa é obrigatória") LocalDateTime completedAt
) {
}
