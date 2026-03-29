package dev.nicolas.JobTracker.application.dto.stage;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateStageRequest(
        @NotBlank(message = "Nome da etapa é obrigatório") String name,
        @NotNull(message = "Ordem da etapa é obrigatória")
        @Min(value = 1, message = "Ordem da etapa deve ser maior que zero") Integer orderIndex,
        LocalDateTime deadlineAt
) {
}
