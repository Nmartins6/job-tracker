package dev.nicolas.JobTracker.application.dto.job;

import jakarta.validation.constraints.NotBlank;

public record CreateJobRequest(
        @NotBlank (message = "Nome da empresa é obrigatório") String company,
        @NotBlank (message = "Titulo precisa ser preenchido") String title,
        String sourceUrl,
        String seniority,
        String location,
        String description
) {
}
