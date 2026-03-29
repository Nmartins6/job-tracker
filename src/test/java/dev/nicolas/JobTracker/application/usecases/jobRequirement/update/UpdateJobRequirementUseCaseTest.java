package dev.nicolas.JobTracker.application.usecases.jobRequirement.update;

import dev.nicolas.JobTracker.application.dto.jobRequirement.JobRequirementResponse;
import dev.nicolas.JobTracker.application.dto.jobRequirement.UpdateJobRequirementRequest;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirement;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirementRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.skill.Skill;
import dev.nicolas.JobTracker.domain.skill.SkillRepository;
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
class UpdateJobRequirementUseCaseTest {

    @Mock
    private JobRequirementRepository jobRequirementRepository;

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private UpdateJobRequirementUseCase updateJobRequirementUseCase;

    @Test
    void shouldUpdateJobRequirementWhenRequestIsValid() {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        UpdateJobRequirementRequest request = new UpdateJobRequirementRequest(skillId, false, 5, 4);

        when(jobRequirementRepository.findById(id)).thenReturn(Optional.of(
                JobRequirement.reconstitute(id, jobId, UUID.randomUUID(), true, 3, 2)
        ));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(Skill.create("Java", "Backend")));
        when(jobRequirementRepository.findByJobIdAndSkillId(jobId, skillId)).thenReturn(Optional.empty());
        when(jobRequirementRepository.save(any(JobRequirement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobRequirementResponse response = updateJobRequirementUseCase.execute(id, request);

        ArgumentCaptor<JobRequirement> captor = ArgumentCaptor.forClass(JobRequirement.class);
        verify(jobRequirementRepository).save(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(id);
        assertThat(captor.getValue().getJobId()).isEqualTo(jobId);
        assertThat(captor.getValue().getSkillId()).isEqualTo(skillId);
        assertThat(captor.getValue().isMustHave()).isFalse();
        assertThat(captor.getValue().getDesiredLevel()).isEqualTo(5);
        assertThat(captor.getValue().getWeight()).isEqualTo(4);

        assertThat(response.skillId()).isEqualTo(skillId);
        assertThat(response.mustHave()).isFalse();
        assertThat(response.desiredLevel()).isEqualTo(5);
        assertThat(response.weight()).isEqualTo(4);
    }

    @Test
    void shouldRejectUpdateWhenRequirementDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(jobRequirementRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateJobRequirementUseCase.execute(
                id,
                new UpdateJobRequirementRequest(UUID.randomUUID(), true, 4, 3)
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Requisito da vaga não encontrado pelo id " + id);

        verify(jobRequirementRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWhenSkillAlreadyExistsForAnotherRequirement() {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        when(jobRequirementRepository.findById(id)).thenReturn(Optional.of(
                JobRequirement.reconstitute(id, jobId, UUID.randomUUID(), true, 3, 2)
        ));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(Skill.create("Java", "Backend")));
        when(jobRequirementRepository.findByJobIdAndSkillId(jobId, skillId)).thenReturn(Optional.of(
                JobRequirement.reconstitute(UUID.randomUUID(), jobId, skillId, true, 4, 3)
        ));

        assertThatThrownBy(() -> updateJobRequirementUseCase.execute(
                id,
                new UpdateJobRequirementRequest(skillId, true, 4, 3)
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Requisito já cadastrado para a vaga");

        verify(jobRequirementRepository, never()).save(any());
    }
}
