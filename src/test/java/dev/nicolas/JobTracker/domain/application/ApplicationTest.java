package dev.nicolas.JobTracker.domain.application;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationTest {

    @Test
    void shouldCreateApplicationWithActiveStatus() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        Application application = Application.create(userId, jobId);

        assertThat(application.getId()).isNotNull();
        assertThat(application.getUserId()).isEqualTo(userId);
        assertThat(application.getJobId()).isEqualTo(jobId);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.ACTIVE);
    }

    @Test
    void shouldUpdateApplicationStatus() {
        Application application = Application.create(UUID.randomUUID(), UUID.randomUUID());

        application.updateStatus(ApplicationStatus.HIRED);

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.HIRED);
    }

    @Test
    void shouldRejectNullStatusUpdate() {
        Application application = Application.create(UUID.randomUUID(), UUID.randomUUID());

        assertThatThrownBy(() -> application.updateStatus(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Status da candidatura é obrigatório");
    }
}
