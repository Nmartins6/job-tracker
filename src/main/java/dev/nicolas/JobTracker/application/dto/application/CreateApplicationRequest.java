package dev.nicolas.JobTracker.application.dto.application;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateApplicationRequest(
        @NotNull(message = "Usuário da candidatura é obrigatório") UUID userId,
        @NotNull(message = "Vaga da candidatura é obrigatória") UUID jobId
) {
}
