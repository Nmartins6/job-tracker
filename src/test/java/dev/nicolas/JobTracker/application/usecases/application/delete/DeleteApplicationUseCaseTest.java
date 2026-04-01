package dev.nicolas.JobTracker.application.usecases.application.delete;

import dev.nicolas.JobTracker.domain.application.Application;
import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
import dev.nicolas.JobTracker.domain.application.ApplicationStatus;
import dev.nicolas.JobTracker.domain.note.Note;
import dev.nicolas.JobTracker.domain.note.NoteRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.stage.Stage;
import dev.nicolas.JobTracker.domain.stage.StageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteApplicationUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private StageRepository stageRepository;

    @InjectMocks
    private DeleteApplicationUseCase deleteApplicationUseCase;

    @Test
    void shouldDeleteApplicationAndTrackingArtifacts() {
        UUID applicationId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(
                Application.reconstitute(
                        applicationId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        ApplicationStatus.ACTIVE,
                        null,
                        null
                )
        ));
        when(noteRepository.findByApplicationId(applicationId)).thenReturn(List.of(
                Note.reconstitute(noteId, applicationId, stageId, "Observacao", LocalDateTime.now())
        ));
        when(stageRepository.findByApplicationId(applicationId)).thenReturn(List.of(
                Stage.reconstitute(stageId, applicationId, "Entrevista", 1, null, null, null)
        ));

        deleteApplicationUseCase.execute(applicationId);

        verify(noteRepository).deleteById(noteId);
        verify(stageRepository).deleteById(stageId);
        verify(applicationRepository).deleteById(applicationId);
    }

    @Test
    void shouldRejectDeleteWhenApplicationDoesNotExist() {
        UUID applicationId = UUID.randomUUID();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteApplicationUseCase.execute(applicationId))
                .isInstanceOf(DomainException.class)
                .hasMessage("Candidatura não encontrada pelo id " + applicationId);

        verify(noteRepository, never()).findByApplicationId(applicationId);
        verify(stageRepository, never()).findByApplicationId(applicationId);
        verify(applicationRepository, never()).deleteById(applicationId);
    }
}
