package dev.nicolas.JobTracker.application.dto.jobRequirement;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateJobRequirementRequest(
        @NotNull(message = "Habilidade do requisito é obrigatória") UUID skillId,
        @NotNull(message = "Campo mustHave é obrigatório") Boolean mustHave,
        @NotNull(message = "Nível desejado é obrigatório")
        @Min(value = 1, message = "Nível desejado deve estar entre 1 e 5")
        @Max(value = 5, message = "Nível desejado deve estar entre 1 e 5") Integer desiredLevel,
        @NotNull(message = "Peso do requisito é obrigatório")
        @Min(value = 1, message = "Peso do requisito deve ser maior que zero") Integer weight
) {
}
