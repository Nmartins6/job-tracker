package dev.nicolas.JobTracker.domain.note;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;

import java.time.LocalDateTime;
import java.util.UUID;

public class Note {

    private UUID id;
    private UUID applicationId;
    private UUID stageId;
    private String content;
    private LocalDateTime createdAt;

    private Note() {

    }

    public static Note create(UUID applicationId, UUID stageId, String content, LocalDateTime createdAt) {
        if (applicationId == null) {
            throw new DomainException("Candidatura da nota é obrigatória");
        }
        if (content == null || content.isBlank()) {
            throw new DomainException("Conteúdo da nota é obrigatório");
        }
        if (createdAt == null) {
            throw new DomainException("Data de criação da nota é obrigatória");
        }

        Note note = new Note();
        note.id = UUID.randomUUID();
        note.applicationId = applicationId;
        note.stageId = stageId;
        note.content = content.trim();
        note.createdAt = createdAt;

        return note;
    }

    public static Note reconstitute(UUID id,
                                    UUID applicationId,
                                    UUID stageId,
                                    String content,
                                    LocalDateTime createdAt) {
        Note note = new Note();
        note.id = id;
        note.applicationId = applicationId;
        note.stageId = stageId;
        note.content = content;
        note.createdAt = createdAt;

        return note;
    }

    public UUID getId() {
        return id;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public UUID getStageId() {
        return stageId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
