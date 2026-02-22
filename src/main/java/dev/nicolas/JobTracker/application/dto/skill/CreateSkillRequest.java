package dev.nicolas.JobTracker.application.dto.skill;

import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;

public record CreateSkillRequest(
        @NotBlank(message = "Nome da habilidade não pode ficar vazio") String name,
        String category) {
}
