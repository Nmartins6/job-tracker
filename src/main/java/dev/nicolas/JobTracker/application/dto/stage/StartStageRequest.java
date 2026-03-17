package dev.nicolas.JobTracker.application.dto.stage;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record StartStageRequest(
        @NotNull(message = "Data de início da etapa é obrigatória") LocalDateTime startedAt
) {
}
