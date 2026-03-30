package dev.nicolas.JobTracker.application.usecases.application.update;

import dev.nicolas.JobTracker.application.dto.application.ApplicationResponse;
import dev.nicolas.JobTracker.application.dto.application.UpdateApplicationRequest;
import dev.nicolas.JobTracker.domain.application.Application;
import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
import dev.nicolas.JobTracker.domain.application.ApplicationStatus;
import dev.nicolas.JobTracker.domain.job.Job;
import dev.nicolas.JobTracker.domain.job.JobRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.user.User;
import dev.nicolas.JobTracker.domain.user.UserRepository;
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
class UpdateApplicationUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private UpdateApplicationUseCase updateApplicationUseCase;

    @Test
    void shouldUpdateApplicationTracking() {
        UUID applicationId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID currentJobId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        UUID newJobId = UUID.randomUUID();

        Application application = Application.reconstitute(
                applicationId,
                currentUserId,
                currentJobId,
                ApplicationStatus.ACTIVE
        );

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(userRepository.findById(newUserId)).thenReturn(Optional.of(User.create(
                "Nicolas Martins",
                "nicolas@example.com",
                "$2a$10$hashedPassword",
                "Backend Engineer",
                "Brasil",
                "Bio"
        )));
        when(jobRepository.findById(newJobId)).thenReturn(Optional.of(Job.reconstitute(
                newJobId,
                "OpenAI",
                "Software Engineer",
                "https://example.com/jobs/1",
                "Senior",
                "Remote",
                "Descricao"
        )));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponse response = updateApplicationUseCase.execute(
                applicationId,
                new UpdateApplicationRequest(newUserId, newJobId, ApplicationStatus.WITHDRAWN)
        );

        ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(applicationCaptor.capture());

        assertThat(applicationCaptor.getValue().getUserId()).isEqualTo(newUserId);
        assertThat(applicationCaptor.getValue().getJobId()).isEqualTo(newJobId);
        assertThat(applicationCaptor.getValue().getStatus()).isEqualTo(ApplicationStatus.WITHDRAWN);
        assertThat(response.userId()).isEqualTo(newUserId);
        assertThat(response.jobId()).isEqualTo(newJobId);
        assertThat(response.status()).isEqualTo(ApplicationStatus.WITHDRAWN);
    }

    @Test
    void shouldThrowWhenApplicationDoesNotExistForUpdate() {
        UUID applicationId = UUID.randomUUID();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateApplicationUseCase.execute(
                applicationId,
                new UpdateApplicationRequest(UUID.randomUUID(), UUID.randomUUID(), ApplicationStatus.ACTIVE)
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Candidatura não encontrada pelo id " + applicationId);

        verify(applicationRepository, never()).save(any(Application.class));
    }
}
