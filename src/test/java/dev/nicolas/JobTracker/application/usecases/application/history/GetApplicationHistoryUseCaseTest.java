package dev.nicolas.JobTracker.application.usecases.application.history;

import dev.nicolas.JobTracker.application.dto.history.ApplicationHistoryEventResponse;
import dev.nicolas.JobTracker.application.dto.history.ApplicationHistoryEventType;
import dev.nicolas.JobTracker.application.dto.history.ApplicationHistoryResponse;
import dev.nicolas.JobTracker.domain.application.Application;
import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetApplicationHistoryUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private StageRepository stageRepository;

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private GetApplicationHistoryUseCase getApplicationHistoryUseCase;

    @Test
    void shouldReturnOrderedApplicationHistory() {
        UUID applicationId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();

        Application application = Application.reconstitute(applicationId, userId, jobId, dev.nicolas.JobTracker.domain.application.ApplicationStatus.ACTIVE);
        Stage stage = Stage.reconstitute(
                stageId,
                applicationId,
                "Entrevista técnica",
                2,
                LocalDateTime.of(2026, 3, 10, 10, 0),
                LocalDateTime.of(2026, 3, 12, 15, 0),
                LocalDateTime.of(2026, 3, 11, 18, 0)
        );
        Note applicationNote = Note.reconstitute(
                UUID.randomUUID(),
                applicationId,
                null,
                "Candidatura criada",
                LocalDateTime.of(2026, 3, 9, 9, 0)
        );
        Note stageNote = Note.reconstitute(
                UUID.randomUUID(),
                applicationId,
                stageId,
                "Bom feedback técnico",
                LocalDateTime.of(2026, 3, 12, 16, 0)
        );

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(stageRepository.findByApplicationId(applicationId)).thenReturn(List.of(stage));
        when(noteRepository.findByApplicationId(applicationId)).thenReturn(List.of(applicationNote, stageNote));

        ApplicationHistoryResponse response = getApplicationHistoryUseCase.execute(applicationId);

        assertThat(response.applicationId()).isEqualTo(applicationId);
        assertThat(response.events()).hasSize(5);
        assertThat(response.events())
                .extracting(ApplicationHistoryEventResponse::type)
                .containsExactly(
                        ApplicationHistoryEventType.NOTE,
                        ApplicationHistoryEventType.STAGE_STARTED,
                        ApplicationHistoryEventType.STAGE_DEADLINE,
                        ApplicationHistoryEventType.STAGE_COMPLETED,
                        ApplicationHistoryEventType.NOTE
                );
        assertThat(response.events().getFirst().title()).isEqualTo("Nota da candidatura");
        assertThat(response.events().getLast().title()).isEqualTo("Nota da etapa Entrevista técnica");
    }

    @Test
    void shouldThrowWhenApplicationDoesNotExist() {
        UUID applicationId = UUID.randomUUID();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getApplicationHistoryUseCase.execute(applicationId))
                .isInstanceOf(DomainException.class)
                .hasMessage("Candidatura não encontrada pelo id " + applicationId);
    }

    @Test
    void shouldReturnEmptyHistoryWhenApplicationHasNoEvents() {
        UUID applicationId = UUID.randomUUID();
        Application application = Application.create(UUID.randomUUID(), UUID.randomUUID());

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(stageRepository.findByApplicationId(applicationId)).thenReturn(List.of());
        when(noteRepository.findByApplicationId(applicationId)).thenReturn(List.of());

        ApplicationHistoryResponse response = getApplicationHistoryUseCase.execute(applicationId);

        assertThat(response.applicationId()).isEqualTo(applicationId);
        assertThat(response.events()).isEmpty();
    }
}
