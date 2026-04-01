package dev.nicolas.JobTracker.application.dto.application;

import dev.nicolas.JobTracker.domain.application.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateApplicationRequest(
        @NotNull(message = "Usuário da candidatura é obrigatório") UUID userId,
        @NotNull(message = "Vaga da candidatura é obrigatória") UUID jobId,
        @NotNull(message = "Status da candidatura é obrigatório") ApplicationStatus status,
        @Size(max = 255, message = "Próxima ação deve ter no máximo 255 caracteres") String nextAction,
        LocalDateTime nextActionDueAt
) {
}
