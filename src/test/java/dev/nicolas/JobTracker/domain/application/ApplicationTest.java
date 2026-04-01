package dev.nicolas.JobTracker.domain.application;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationTest {

    @Test
    void shouldCreateApplicationWithActiveStatus() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        Application application = Application.create(userId, jobId, null, null);

        assertThat(application.getId()).isNotNull();
        assertThat(application.getUserId()).isEqualTo(userId);
        assertThat(application.getJobId()).isEqualTo(jobId);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.ACTIVE);
    }

    @Test
    void shouldUpdateApplicationStatus() {
        Application application = Application.create(UUID.randomUUID(), UUID.randomUUID(), null, null);

        application.updateStatus(ApplicationStatus.HIRED);

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.HIRED);
    }

    @Test
    void shouldRejectNullStatusUpdate() {
        Application application = Application.create(UUID.randomUUID(), UUID.randomUUID(), null, null);

        assertThatThrownBy(() -> application.updateStatus(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Status da candidatura é obrigatório");
    }

    @Test
    void shouldKeepManualNextActionWhenCreatingApplication() {
        LocalDateTime dueAt = LocalDateTime.of(2026, 4, 2, 9, 0);

        Application application = Application.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Cobrar retorno da recrutadora",
                dueAt
        );

        assertThat(application.getNextAction()).isEqualTo("Cobrar retorno da recrutadora");
        assertThat(application.getNextActionDueAt()).isEqualTo(dueAt);
    }

    @Test
    void shouldRejectNextActionDueDateWithoutNextAction() {
        assertThatThrownBy(() -> Application.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "   ",
                LocalDateTime.of(2026, 4, 2, 9, 0)
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Próxima ação é obrigatória quando a data é informada");
    }
}
