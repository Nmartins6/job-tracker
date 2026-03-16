package dev.nicolas.JobTracker.application.usecases.application.update;

import dev.nicolas.JobTracker.application.dto.application.ApplicationResponse;
import dev.nicolas.JobTracker.application.dto.application.UpdateApplicationStatusRequest;
import dev.nicolas.JobTracker.domain.application.Application;
import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
import dev.nicolas.JobTracker.domain.application.ApplicationStatus;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
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
class UpdateApplicationStatusUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private UpdateApplicationStatusUseCase updateApplicationStatusUseCase;

    @Test
    void shouldUpdateApplicationStatus() {
        UUID id = UUID.randomUUID();
        Application application = Application.reconstitute(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                ApplicationStatus.ACTIVE
        );

        when(applicationRepository.findById(id)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponse response = updateApplicationStatusUseCase.execute(
                id,
                new UpdateApplicationStatusRequest(ApplicationStatus.HIRED)
        );

        ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(applicationCaptor.capture());

        assertThat(applicationCaptor.getValue().getStatus()).isEqualTo(ApplicationStatus.HIRED);
        assertThat(response.status()).isEqualTo(ApplicationStatus.HIRED);
    }

    @Test
    void shouldThrowWhenApplicationIsNotFoundForStatusUpdate() {
        UUID id = UUID.randomUUID();

        when(applicationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateApplicationStatusUseCase.execute(
                id,
                new UpdateApplicationStatusRequest(ApplicationStatus.REJECTED)
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Candidatura não encontrada pelo id " + id);

        verify(applicationRepository, never()).save(any(Application.class));
    }
}
