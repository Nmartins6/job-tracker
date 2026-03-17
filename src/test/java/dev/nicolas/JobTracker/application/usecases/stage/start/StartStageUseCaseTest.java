package dev.nicolas.JobTracker.application.usecases.stage.start;

import dev.nicolas.JobTracker.application.dto.stage.StageResponse;
import dev.nicolas.JobTracker.application.dto.stage.StartStageRequest;
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
class StartStageUseCaseTest {

    @Mock
    private StageRepository stageRepository;

    @InjectMocks
    private StartStageUseCase startStageUseCase;

    @Test
    void shouldStartStage() {
        UUID id = UUID.randomUUID();
        LocalDateTime startedAt = LocalDateTime.now();
        Stage stage = Stage.reconstitute(
                id,
                UUID.randomUUID(),
                "Screening",
                1,
                null,
                null,
                null
        );

        when(stageRepository.findById(id)).thenReturn(Optional.of(stage));
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StageResponse response = startStageUseCase.execute(id, new StartStageRequest(startedAt));

        ArgumentCaptor<Stage> stageCaptor = ArgumentCaptor.forClass(Stage.class);
        verify(stageRepository).save(stageCaptor.capture());

        assertThat(stageCaptor.getValue().getStartedAt()).isEqualTo(startedAt);
        assertThat(response.startedAt()).isEqualTo(startedAt);
    }

    @Test
    void shouldThrowWhenStageDoesNotExistForStart() {
        UUID id = UUID.randomUUID();

        when(stageRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> startStageUseCase.execute(id, new StartStageRequest(LocalDateTime.now())))
                .isInstanceOf(DomainException.class)
                .hasMessage("Etapa não encontrada pelo id " + id);

        verify(stageRepository, never()).save(any(Stage.class));
    }
}
