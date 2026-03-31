package dev.nicolas.JobTracker.application.usecases.job.delete;

import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
import dev.nicolas.JobTracker.domain.job.Job;
import dev.nicolas.JobTracker.domain.job.JobRepository;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirement;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirementRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteJobUseCaseTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRequirementRepository jobRequirementRepository;

    @InjectMocks
    private DeleteJobUseCase deleteJobUseCase;

    @Test
    void shouldDeleteJobAndRequirementsWhenNoApplicationsReferenceIt() {
        UUID jobId = UUID.randomUUID();
        UUID requirementId = UUID.randomUUID();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(Job.reconstitute(
                jobId,
                "Acme",
                "Backend Engineer",
                "https://example.com",
                "Pleno",
                "Remote",
                "Descricao"
        )));
        when(applicationRepository.existsByJobId(jobId)).thenReturn(false);
        when(jobRequirementRepository.findByJobId(jobId)).thenReturn(List.of(
                JobRequirement.reconstitute(requirementId, jobId, UUID.randomUUID(), true, 4, 5)
        ));

        deleteJobUseCase.execute(jobId);

        verify(jobRequirementRepository).deleteById(requirementId);
        verify(jobRepository).deleteById(jobId);
    }

    @Test
    void shouldRejectDeleteWhenJobDoesNotExist() {
        UUID jobId = UUID.randomUUID();

        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteJobUseCase.execute(jobId))
                .isInstanceOf(DomainException.class)
                .hasMessage("Vaga não encontrada pelo id " + jobId);

        verify(jobRequirementRepository, never()).findByJobId(jobId);
        verify(jobRepository, never()).deleteById(jobId);
    }

    @Test
    void shouldRejectDeleteWhenJobHasTrackedApplications() {
        UUID jobId = UUID.randomUUID();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(Job.reconstitute(
                jobId,
                "Acme",
                "Backend Engineer",
                null,
                null,
                null,
                null
        )));
        when(applicationRepository.existsByJobId(jobId)).thenReturn(true);

        assertThatThrownBy(() -> deleteJobUseCase.execute(jobId))
                .isInstanceOf(DomainException.class)
                .hasMessage("Não é possível remover vaga com candidaturas vinculadas");

        verify(jobRequirementRepository, never()).findByJobId(jobId);
        verify(jobRepository, never()).deleteById(jobId);
    }
}
