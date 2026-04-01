package dev.nicolas.JobTracker.application.usecases.application.create;

import dev.nicolas.JobTracker.application.dto.application.ApplicationResponse;
import dev.nicolas.JobTracker.application.dto.application.CreateApplicationRequest;
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
class CreateApplicationUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private CreateApplicationUseCase createApplicationUseCase;

    @Test
    void shouldCreateApplicationWhenUserAndJobExist() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        LocalDateTime nextActionDueAt = LocalDateTime.of(2026, 4, 2, 10, 0);
        CreateApplicationRequest request = new CreateApplicationRequest(
                userId,
                jobId,
                "Enviar follow-up",
                nextActionDueAt
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(User.create(
                "Nicolas",
                "nicolas@example.com",
                "123456",
                "Backend Developer",
                "Brazil",
                "Bio"
        )));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(Job.create(
                "Acme",
                "Backend Engineer",
                null,
                "Mid",
                "Remote",
                "Description"
        )));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponse response = createApplicationUseCase.execute(request);

        ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(applicationCaptor.capture());

        Application savedApplication = applicationCaptor.getValue();
        assertThat(savedApplication.getUserId()).isEqualTo(userId);
        assertThat(savedApplication.getJobId()).isEqualTo(jobId);
        assertThat(savedApplication.getStatus()).isEqualTo(ApplicationStatus.ACTIVE);
        assertThat(savedApplication.getNextAction()).isEqualTo("Enviar follow-up");
        assertThat(savedApplication.getNextActionDueAt()).isEqualTo(nextActionDueAt);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.jobId()).isEqualTo(jobId);
        assertThat(response.status()).isEqualTo(ApplicationStatus.ACTIVE);
        assertThat(response.nextAction()).isEqualTo("Enviar follow-up");
        assertThat(response.nextActionDueAt()).isEqualTo(nextActionDueAt);
    }

    @Test
    void shouldRejectApplicationWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createApplicationUseCase.execute(new CreateApplicationRequest(userId, jobId, null, null)))
                .isInstanceOf(DomainException.class)
                .hasMessage("Usuário não encontrado pelo id " + userId);

        verify(jobRepository, never()).findById(jobId);
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void shouldRejectApplicationWhenJobDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(User.create(
                "Nicolas",
                "nicolas@example.com",
                "123456",
                "Backend Developer",
                "Brazil",
                "Bio"
        )));
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createApplicationUseCase.execute(new CreateApplicationRequest(userId, jobId, null, null)))
                .isInstanceOf(DomainException.class)
                .hasMessage("Vaga não encontrada pelo id " + jobId);

        verify(applicationRepository, never()).save(any(Application.class));
    }
}
