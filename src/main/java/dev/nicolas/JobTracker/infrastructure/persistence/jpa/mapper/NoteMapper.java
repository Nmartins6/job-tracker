package dev.nicolas.JobTracker.infrastructure.persistence.jpa.mapper;

import dev.nicolas.JobTracker.domain.note.Note;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.NoteJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    public NoteJpaEntity toJpaEntity(Note note) {
        return NoteJpaEntity.builder()
                .id(note.getId())
                .applicationId(note.getApplicationId())
                .stageId(note.getStageId())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .build();
    }

    public Note toDomain(NoteJpaEntity entity) {
        return Note.reconstitute(
                entity.getId(),
                entity.getApplicationId(),
                entity.getStageId(),
                entity.getContent(),
                entity.getCreatedAt()
        );
    }
}
