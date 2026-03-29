package dev.nicolas.JobTracker.application.usecases.stage.update;

import dev.nicolas.JobTracker.application.dto.stage.StageResponse;
import dev.nicolas.JobTracker.application.dto.stage.UpdateStageRequest;
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
class UpdateStageUseCaseTest {

    @Mock
    private StageRepository stageRepository;

    @InjectMocks
    private UpdateStageUseCase updateStageUseCase;

    @Test
    void shouldUpdateStageWhenRequestIsValid() {
        UUID id = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        LocalDateTime startedAt = LocalDateTime.now().minusHours(2);
        LocalDateTime completedAt = null;
        LocalDateTime deadlineAt = LocalDateTime.now().plusDays(3);

        when(stageRepository.findById(id)).thenReturn(Optional.of(
                Stage.reconstitute(id, applicationId, "Screening", 1, startedAt, completedAt, null)
        ));
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StageResponse response = updateStageUseCase.execute(
                id,
                new UpdateStageRequest("Entrevista inicial", 2, deadlineAt)
        );

        ArgumentCaptor<Stage> captor = ArgumentCaptor.forClass(Stage.class);
        verify(stageRepository).save(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(id);
        assertThat(captor.getValue().getApplicationId()).isEqualTo(applicationId);
        assertThat(captor.getValue().getName()).isEqualTo("Entrevista inicial");
        assertThat(captor.getValue().getOrderIndex()).isEqualTo(2);
        assertThat(captor.getValue().getStartedAt()).isEqualTo(startedAt);
        assertThat(captor.getValue().getDeadlineAt()).isEqualTo(deadlineAt);

        assertThat(response.name()).isEqualTo("Entrevista inicial");
        assertThat(response.orderIndex()).isEqualTo(2);
        assertThat(response.deadlineAt()).isEqualTo(deadlineAt);
    }

    @Test
    void shouldRejectUpdateWhenStageDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(stageRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateStageUseCase.execute(
                id,
                new UpdateStageRequest("Entrevista", 2, LocalDateTime.now().plusDays(1))
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Etapa não encontrada pelo id " + id);

        verify(stageRepository, never()).save(any());
    }
}
