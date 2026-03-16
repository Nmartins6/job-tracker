package dev.nicolas.JobTracker.application.dto.application;

import dev.nicolas.JobTracker.domain.application.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateApplicationStatusRequest(
        @NotNull(message = "Status da candidatura é obrigatório") ApplicationStatus status
) {
}
