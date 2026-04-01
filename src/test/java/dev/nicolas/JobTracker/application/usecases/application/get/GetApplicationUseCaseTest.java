package dev.nicolas.JobTracker.application.usecases.application.get;

import dev.nicolas.JobTracker.application.dto.application.ApplicationResponse;
import dev.nicolas.JobTracker.domain.application.Application;
import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
import dev.nicolas.JobTracker.domain.application.ApplicationStatus;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
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
class GetApplicationUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private GetApplicationUseCase getApplicationUseCase;

    @Test
    void shouldReturnApplicationById() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        LocalDateTime nextActionDueAt = LocalDateTime.of(2026, 4, 3, 14, 0);
        Application application = Application.reconstitute(
                id,
                userId,
                jobId,
                ApplicationStatus.ACTIVE,
                "Cobrar retorno por email",
                nextActionDueAt
        );

        when(applicationRepository.findById(id)).thenReturn(Optional.of(application));

        ApplicationResponse response = getApplicationUseCase.findById(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.jobId()).isEqualTo(jobId);
        assertThat(response.status()).isEqualTo(ApplicationStatus.ACTIVE);
        assertThat(response.nextAction()).isEqualTo("Cobrar retorno por email");
        assertThat(response.nextActionDueAt()).isEqualTo(nextActionDueAt);
    }

    @Test
    void shouldThrowWhenApplicationIsNotFoundById() {
        UUID id = UUID.randomUUID();

        when(applicationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getApplicationUseCase.findById(id))
                .isInstanceOf(DomainException.class)
                .hasMessage("Candidatura não encontrada pelo id " + id);
    }

    @Test
    void shouldReturnAllApplications() {
        Application first = Application.reconstitute(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                ApplicationStatus.ACTIVE,
                null,
                null
        );
        Application second = Application.reconstitute(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                ApplicationStatus.REJECTED,
                "Registrar feedback",
                LocalDateTime.of(2026, 4, 3, 18, 30)
        );

        when(applicationRepository.findAll()).thenReturn(List.of(first, second));

        List<ApplicationResponse> responses = getApplicationUseCase.findAll();

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(ApplicationResponse::status)
                .containsExactly(ApplicationStatus.ACTIVE, ApplicationStatus.REJECTED);
        assertThat(responses.get(1).nextAction()).isEqualTo("Registrar feedback");
    }
}
