package dev.nicolas.JobTracker.application.dto.note;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UpdateNoteRequest(
        UUID stageId,
        @NotBlank(message = "Conteúdo da nota é obrigatório") String content
) {
}
