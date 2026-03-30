package dev.nicolas.JobTracker.application.dto.application;

import dev.nicolas.JobTracker.domain.application.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateApplicationRequest(
        @NotNull(message = "Usuário da candidatura é obrigatório") UUID userId,
        @NotNull(message = "Vaga da candidatura é obrigatória") UUID jobId,
        @NotNull(message = "Status da candidatura é obrigatório") ApplicationStatus status
) {
}
