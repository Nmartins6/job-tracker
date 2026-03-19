package dev.nicolas.JobTracker.application.usecases.jobRequirement.create;

import dev.nicolas.JobTracker.application.dto.jobRequirement.CreateJobRequirementRequest;
import dev.nicolas.JobTracker.application.dto.jobRequirement.JobRequirementResponse;
import dev.nicolas.JobTracker.domain.job.Job;
import dev.nicolas.JobTracker.domain.job.JobRepository;
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
class CreateJobRequirementUseCaseTest {

    @Mock
    private JobRequirementRepository jobRequirementRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private CreateJobRequirementUseCase createJobRequirementUseCase;

    @Test
    void shouldCreateJobRequirementWhenJobAndSkillExist() {
        UUID jobId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        CreateJobRequirementRequest request = new CreateJobRequirementRequest(jobId, skillId, true, 4, 3);

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(Job.create(
                "Acme",
                "Backend Engineer",
                null,
                "Mid",
                "Remote",
                "Description"
        )));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(Skill.create("Java", "Backend")));
        when(jobRequirementRepository.findByJobIdAndSkillId(jobId, skillId)).thenReturn(Optional.empty());
        when(jobRequirementRepository.save(any(JobRequirement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobRequirementResponse response = createJobRequirementUseCase.execute(request);

        ArgumentCaptor<JobRequirement> requirementCaptor = ArgumentCaptor.forClass(JobRequirement.class);
        verify(jobRequirementRepository).save(requirementCaptor.capture());

        JobRequirement savedRequirement = requirementCaptor.getValue();
        assertThat(savedRequirement.getJobId()).isEqualTo(jobId);
        assertThat(savedRequirement.getSkillId()).isEqualTo(skillId);
        assertThat(savedRequirement.isMustHave()).isTrue();
        assertThat(savedRequirement.getDesiredLevel()).isEqualTo(4);
        assertThat(savedRequirement.getWeight()).isEqualTo(3);

        assertThat(response.jobId()).isEqualTo(jobId);
        assertThat(response.skillId()).isEqualTo(skillId);
        assertThat(response.mustHave()).isTrue();
        assertThat(response.desiredLevel()).isEqualTo(4);
        assertThat(response.weight()).isEqualTo(3);
    }

    @Test
    void shouldRejectJobRequirementWhenJobDoesNotExist() {
        UUID jobId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createJobRequirementUseCase.execute(
                new CreateJobRequirementRequest(jobId, skillId, true, 4, 3)
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Vaga não encontrada pelo id " + jobId);

        verify(skillRepository, never()).findById(skillId);
        verify(jobRequirementRepository, never()).save(any(JobRequirement.class));
    }

    @Test
    void shouldRejectJobRequirementWhenSkillDoesNotExist() {
        UUID jobId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(Job.create(
                "Acme",
                "Backend Engineer",
                null,
                "Mid",
                "Remote",
                "Description"
        )));
        when(skillRepository.findById(skillId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createJobRequirementUseCase.execute(
                new CreateJobRequirementRequest(jobId, skillId, true, 4, 3)
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Habilidade não encontrada pelo id " + skillId);

        verify(jobRequirementRepository, never()).save(any(JobRequirement.class));
    }

    @Test
    void shouldRejectJobRequirementWhenSkillAlreadyExistsForJob() {
        UUID jobId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(Job.create(
                "Acme",
                "Backend Engineer",
                null,
                "Mid",
                "Remote",
                "Description"
        )));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(Skill.create("Java", "Backend")));
        when(jobRequirementRepository.findByJobIdAndSkillId(jobId, skillId)).thenReturn(
                Optional.of(JobRequirement.reconstitute(UUID.randomUUID(), jobId, skillId, true, 4, 3))
        );

        assertThatThrownBy(() -> createJobRequirementUseCase.execute(
                new CreateJobRequirementRequest(jobId, skillId, true, 4, 3)
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Requisito já cadastrado para a vaga");

        verify(jobRequirementRepository, never()).save(any(JobRequirement.class));
    }
}
