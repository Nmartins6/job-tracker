package dev.nicolas.JobTracker.application.dto.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateApplicationRequest(
        @NotNull(message = "Usuário da candidatura é obrigatório") UUID userId,
        @NotNull(message = "Vaga da candidatura é obrigatória") UUID jobId,
        @Size(max = 255, message = "Próxima ação deve ter no máximo 255 caracteres") String nextAction,
        LocalDateTime nextActionDueAt
) {
}
