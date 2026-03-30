package dev.nicolas.JobTracker.application.usecases.job.update;

import dev.nicolas.JobTracker.application.dto.job.JobResponse;
import dev.nicolas.JobTracker.application.dto.job.UpdateJobRequest;
import dev.nicolas.JobTracker.domain.job.Job;
import dev.nicolas.JobTracker.domain.job.JobRepository;
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
class UpdateJobUseCaseTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private UpdateJobUseCase updateJobUseCase;

    @Test
    void shouldUpdateJobWhenRequestIsValid() {
        UUID id = UUID.randomUUID();
        UpdateJobRequest request = new UpdateJobRequest(
                "Acme",
                "Senior Backend Engineer",
                "https://example.com/job",
                "Senior",
                "Remoto",
                "Vaga atualizada"
        );

        when(jobRepository.findById(id)).thenReturn(Optional.of(
                Job.reconstitute(id, "Old Company", "Old Title", null, null, null, null)
        ));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobResponse response = updateJobUseCase.execute(id, request);

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(id);
        assertThat(captor.getValue().getCompany()).isEqualTo("Acme");
        assertThat(captor.getValue().getTitle()).isEqualTo("Senior Backend Engineer");
        assertThat(captor.getValue().getSourceUrl()).isEqualTo("https://example.com/job");
        assertThat(captor.getValue().getSeniority()).isEqualTo("Senior");
        assertThat(captor.getValue().getLocation()).isEqualTo("Remoto");
        assertThat(captor.getValue().getDescription()).isEqualTo("Vaga atualizada");

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.company()).isEqualTo("Acme");
        assertThat(response.title()).isEqualTo("Senior Backend Engineer");
    }

    @Test
    void shouldRejectUpdateWhenJobDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(jobRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateJobUseCase.execute(
                id,
                new UpdateJobRequest("Acme", "Senior Backend Engineer", null, null, null, null)
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Vaga não encontrada pelo id " + id);

        verify(jobRepository, never()).save(any());
    }
}
