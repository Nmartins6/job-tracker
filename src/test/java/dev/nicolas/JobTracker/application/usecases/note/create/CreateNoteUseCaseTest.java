package dev.nicolas.JobTracker.application.usecases.note.create;

import dev.nicolas.JobTracker.application.dto.note.CreateNoteRequest;
import dev.nicolas.JobTracker.application.dto.note.NoteResponse;
import dev.nicolas.JobTracker.domain.application.Application;
import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateNoteUseCaseTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private StageRepository stageRepository;

    @InjectMocks
    private CreateNoteUseCase createNoteUseCase;

    @Test
    void shouldCreateApplicationLevelNoteWhenApplicationExists() {
        UUID applicationId = UUID.randomUUID();
        CreateNoteRequest request = new CreateNoteRequest(applicationId, null, "Observação da candidatura");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(
                Application.create(UUID.randomUUID(), UUID.randomUUID())
        ));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NoteResponse response = createNoteUseCase.execute(request);

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(noteCaptor.capture());

        Note savedNote = noteCaptor.getValue();
        assertThat(savedNote.getApplicationId()).isEqualTo(applicationId);
        assertThat(savedNote.getStageId()).isNull();
        assertThat(savedNote.getContent()).isEqualTo("Observação da candidatura");
        assertThat(savedNote.getCreatedAt()).isNotNull();

        assertThat(response.applicationId()).isEqualTo(applicationId);
        assertThat(response.stageId()).isNull();
        assertThat(response.content()).isEqualTo("Observação da candidatura");
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void shouldCreateStageLevelNoteWhenStageBelongsToApplication() {
        UUID applicationId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        CreateNoteRequest request = new CreateNoteRequest(applicationId, stageId, "Feedback da etapa");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(
                Application.create(UUID.randomUUID(), UUID.randomUUID())
        ));
        when(stageRepository.findById(stageId)).thenReturn(Optional.of(
                Stage.reconstitute(stageId, applicationId, "Screening", 1, null, null, null)
        ));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NoteResponse response = createNoteUseCase.execute(request);

        assertThat(response.applicationId()).isEqualTo(applicationId);
        assertThat(response.stageId()).isEqualTo(stageId);
        assertThat(response.content()).isEqualTo("Feedback da etapa");
    }

    @Test
    void shouldRejectNoteWhenApplicationDoesNotExist() {
        UUID applicationId = UUID.randomUUID();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createNoteUseCase.execute(
                new CreateNoteRequest(applicationId, null, "Conteúdo")
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Candidatura não encontrada pelo id " + applicationId);

        verify(stageRepository, never()).findById(any());
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void shouldRejectNoteWhenStageDoesNotExist() {
        UUID applicationId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(
                Application.create(UUID.randomUUID(), UUID.randomUUID())
        ));
        when(stageRepository.findById(stageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createNoteUseCase.execute(
                new CreateNoteRequest(applicationId, stageId, "Conteúdo")
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Etapa não encontrada pelo id " + stageId);

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void shouldRejectNoteWhenStageDoesNotBelongToApplication() {
        UUID applicationId = UUID.randomUUID();
        UUID otherApplicationId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(
                Application.create(UUID.randomUUID(), UUID.randomUUID())
        ));
        when(stageRepository.findById(stageId)).thenReturn(Optional.of(
                Stage.reconstitute(stageId, otherApplicationId, "Screening", 1, null, null, null)
        ));

        assertThatThrownBy(() -> createNoteUseCase.execute(
                new CreateNoteRequest(applicationId, stageId, "Conteúdo")
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Etapa não pertence à candidatura informada");

        verify(noteRepository, never()).save(any(Note.class));
    }
}
