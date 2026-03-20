package dev.nicolas.JobTracker.domain.note;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NoteTest {

    @Test
    void shouldCreateNoteWithApplicationAndOptionalStage() {
        UUID applicationId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        Note note = Note.create(applicationId, stageId, "Feedback positivo da entrevista", createdAt);

        assertThat(note.getId()).isNotNull();
        assertThat(note.getApplicationId()).isEqualTo(applicationId);
        assertThat(note.getStageId()).isEqualTo(stageId);
        assertThat(note.getContent()).isEqualTo("Feedback positivo da entrevista");
        assertThat(note.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldCreateApplicationLevelNoteWithoutStage() {
        UUID applicationId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        Note note = Note.create(applicationId, null, "Observação geral da candidatura", createdAt);

        assertThat(note.getStageId()).isNull();
        assertThat(note.getApplicationId()).isEqualTo(applicationId);
    }

    @Test
    void shouldRejectNoteWithoutApplicationId() {
        assertThatThrownBy(() -> Note.create(null, UUID.randomUUID(), "Conteúdo", LocalDateTime.now()))
                .isInstanceOf(DomainException.class)
                .hasMessage("Candidatura da nota é obrigatória");
    }

    @Test
    void shouldRejectNoteWithoutContent() {
        assertThatThrownBy(() -> Note.create(UUID.randomUUID(), null, "   ", LocalDateTime.now()))
                .isInstanceOf(DomainException.class)
                .hasMessage("Conteúdo da nota é obrigatório");
    }

    @Test
    void shouldRejectNoteWithoutCreatedAt() {
        assertThatThrownBy(() -> Note.create(UUID.randomUUID(), null, "Conteúdo", null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Data de criação da nota é obrigatória");
    }
}
