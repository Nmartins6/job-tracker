package dev.nicolas.JobTracker.application.usecases.jobRequirement.get;

import dev.nicolas.JobTracker.application.dto.jobRequirement.JobRequirementResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetJobRequirementUseCaseTest {

    @Mock
    private JobRequirementRepository jobRequirementRepository;

    @InjectMocks
    private GetJobRequirementUseCase getJobRequirementUseCase;

    @Test
    void shouldReturnJobRequirementById() {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        JobRequirement jobRequirement = JobRequirement.reconstitute(id, jobId, skillId, true, 4, 3);

        when(jobRequirementRepository.findById(id)).thenReturn(Optional.of(jobRequirement));

        JobRequirementResponse response = getJobRequirementUseCase.findById(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.jobId()).isEqualTo(jobId);
        assertThat(response.skillId()).isEqualTo(skillId);
        assertThat(response.mustHave()).isTrue();
        assertThat(response.desiredLevel()).isEqualTo(4);
        assertThat(response.weight()).isEqualTo(3);
    }

    @Test
    void shouldThrowWhenJobRequirementIsNotFoundById() {
        UUID id = UUID.randomUUID();

        when(jobRequirementRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getJobRequirementUseCase.findById(id))
                .isInstanceOf(DomainException.class)
                .hasMessage("Requisito da vaga não encontrado pelo id " + id);
    }

    @Test
    void shouldReturnJobRequirementsByJobId() {
        UUID jobId = UUID.randomUUID();
        JobRequirement first = JobRequirement.reconstitute(UUID.randomUUID(), jobId, UUID.randomUUID(), true, 4, 5);
        JobRequirement second = JobRequirement.reconstitute(UUID.randomUUID(), jobId, UUID.randomUUID(), false, 3, 2);

        when(jobRequirementRepository.findByJobId(jobId)).thenReturn(List.of(first, second));

        List<JobRequirementResponse> responses = getJobRequirementUseCase.findByJobId(jobId);

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(JobRequirementResponse::weight)
                .containsExactly(5, 2);
    }
}
