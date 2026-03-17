package dev.nicolas.JobTracker.domain.stage;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StageTest {

    @Test
    void shouldCreateStageWithBasicData() {
        UUID applicationId = UUID.randomUUID();
        LocalDateTime deadlineAt = LocalDateTime.now().plusDays(2);

        Stage stage = Stage.create(applicationId, "Screening", 1, deadlineAt);

        assertThat(stage.getId()).isNotNull();
        assertThat(stage.getApplicationId()).isEqualTo(applicationId);
        assertThat(stage.getName()).isEqualTo("Screening");
        assertThat(stage.getOrderIndex()).isEqualTo(1);
        assertThat(stage.getStartedAt()).isNull();
        assertThat(stage.getCompletedAt()).isNull();
        assertThat(stage.getDeadlineAt()).isEqualTo(deadlineAt);
    }

    @Test
    void shouldStartAndCompleteStage() {
        Stage stage = Stage.create(UUID.randomUUID(), "Technical Interview", 2, null);
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime completedAt = startedAt.plusHours(1);

        stage.start(startedAt);
        stage.complete(completedAt);

        assertThat(stage.getStartedAt()).isEqualTo(startedAt);
        assertThat(stage.getCompletedAt()).isEqualTo(completedAt);
    }

    @Test
    void shouldRejectCompletionBeforeStart() {
        Stage stage = Stage.create(UUID.randomUUID(), "Challenge", 3, null);

        assertThatThrownBy(() -> stage.complete(LocalDateTime.now()))
                .isInstanceOf(DomainException.class)
                .hasMessage("Etapa precisa ser iniciada antes de ser concluída");
    }

    @Test
    void shouldRejectInvalidOrderIndex() {
        assertThatThrownBy(() -> Stage.create(UUID.randomUUID(), "Offer", 0, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Ordem da etapa deve ser maior que zero");
    }
}
