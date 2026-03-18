package dev.nicolas.JobTracker.application.dto.userSkill;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateUserSkillRequest(
        @NotNull(message = "Usuário da habilidade é obrigatório") UUID userId,
        @NotNull(message = "Habilidade é obrigatória") UUID skillId,
        @NotNull(message = "Anos de experiência é obrigatório")
        @Min(value = 0, message = "Anos de experiência não pode ser negativo") Integer yearsExperience,
        @NotNull(message = "Nível da habilidade é obrigatório")
        @Min(value = 1, message = "Nível da habilidade deve estar entre 1 e 5")
        @Max(value = 5, message = "Nível da habilidade deve estar entre 1 e 5") Integer level
) {
}
