package dev.nicolas.JobTracker.application.usecases.note.update;

import dev.nicolas.JobTracker.application.dto.note.NoteResponse;
import dev.nicolas.JobTracker.application.dto.note.UpdateNoteRequest;
import dev.nicolas.JobTracker.domain.note.Note;
import dev.nicolas.JobTracker.domain.note.NoteRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.stage.Stage;
import dev.nicolas.JobTracker.domain.stage.StageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateNoteUseCaseTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private StageRepository stageRepository;

    @InjectMocks
    private UpdateNoteUseCase updateNoteUseCase;

    @Test
    void shouldUpdateNoteWhenRequestIsValid() {
        UUID id = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

        when(noteRepository.findById(id)).thenReturn(Optional.of(
                Note.reconstitute(id, applicationId, null, "Conteúdo antigo", createdAt)
        ));
        when(stageRepository.findById(stageId)).thenReturn(Optional.of(
                Stage.reconstitute(stageId, applicationId, "Screening", 1, null, null, null)
        ));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NoteResponse response = updateNoteUseCase.execute(id, new UpdateNoteRequest(stageId, "Conteúdo novo"));

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(id);
        assertThat(captor.getValue().getApplicationId()).isEqualTo(applicationId);
        assertThat(captor.getValue().getStageId()).isEqualTo(stageId);
        assertThat(captor.getValue().getContent()).isEqualTo("Conteúdo novo");
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(createdAt);

        assertThat(response.stageId()).isEqualTo(stageId);
        assertThat(response.content()).isEqualTo("Conteúdo novo");
    }

    @Test
    void shouldRejectUpdateWhenNoteDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(noteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateNoteUseCase.execute(id, new UpdateNoteRequest(null, "Conteúdo")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Nota não encontrada pelo id " + id);

        verify(noteRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWhenStageBelongsToAnotherApplication() {
        UUID id = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID otherApplicationId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();

        when(noteRepository.findById(id)).thenReturn(Optional.of(
                Note.reconstitute(id, applicationId, null, "Conteúdo antigo", LocalDateTime.now())
        ));
        when(stageRepository.findById(stageId)).thenReturn(Optional.of(
                Stage.reconstitute(stageId, otherApplicationId, "Entrevista", 2, null, null, null)
        ));

        assertThatThrownBy(() -> updateNoteUseCase.execute(id, new UpdateNoteRequest(stageId, "Conteúdo novo")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Etapa não pertence à candidatura informada");

        verify(noteRepository, never()).save(any());
    }
}
