package dev.nicolas.JobTracker.application.dto.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateNoteRequest(
        @NotNull(message = "Candidatura da nota é obrigatória") UUID applicationId,
        UUID stageId,
        @NotBlank(message = "Conteúdo da nota é obrigatório") String content
) {
}
