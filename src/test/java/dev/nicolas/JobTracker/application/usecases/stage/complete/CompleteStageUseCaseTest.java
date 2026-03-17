package dev.nicolas.JobTracker.application.usecases.stage.complete;

import dev.nicolas.JobTracker.application.dto.stage.CompleteStageRequest;
import dev.nicolas.JobTracker.application.dto.stage.StageResponse;
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
class CompleteStageUseCaseTest {

    @Mock
    private StageRepository stageRepository;

    @InjectMocks
    private CompleteStageUseCase completeStageUseCase;

    @Test
    void shouldCompleteStage() {
        UUID id = UUID.randomUUID();
        LocalDateTime startedAt = LocalDateTime.now().minusHours(2);
        LocalDateTime completedAt = startedAt.plusHours(1);
        Stage stage = Stage.reconstitute(
                id,
                UUID.randomUUID(),
                "Technical Interview",
                2,
                startedAt,
                null,
                null
        );

        when(stageRepository.findById(id)).thenReturn(Optional.of(stage));
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StageResponse response = completeStageUseCase.execute(id, new CompleteStageRequest(completedAt));

        ArgumentCaptor<Stage> stageCaptor = ArgumentCaptor.forClass(Stage.class);
        verify(stageRepository).save(stageCaptor.capture());

        assertThat(stageCaptor.getValue().getCompletedAt()).isEqualTo(completedAt);
        assertThat(response.completedAt()).isEqualTo(completedAt);
    }

    @Test
    void shouldThrowWhenStageDoesNotExistForCompletion() {
        UUID id = UUID.randomUUID();

        when(stageRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completeStageUseCase.execute(id, new CompleteStageRequest(LocalDateTime.now())))
                .isInstanceOf(DomainException.class)
                .hasMessage("Etapa não encontrada pelo id " + id);

        verify(stageRepository, never()).save(any(Stage.class));
    }
}
