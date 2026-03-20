package dev.nicolas.JobTracker.application.usecases.matching.get;

import dev.nicolas.JobTracker.application.dto.matching.JobMatchingResponse;
import dev.nicolas.JobTracker.application.service.matching.JobMatchingService;
import dev.nicolas.JobTracker.domain.job.Job;
import dev.nicolas.JobTracker.domain.job.JobRepository;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirement;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirementRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.skill.Skill;
import dev.nicolas.JobTracker.domain.user.User;
import dev.nicolas.JobTracker.domain.user.UserRepository;
import dev.nicolas.JobTracker.domain.userSkill.UserSkill;
import dev.nicolas.JobTracker.domain.userSkill.UserSkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetJobMatchingUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserSkillRepository userSkillRepository;

    @Mock
    private JobRequirementRepository jobRequirementRepository;

    @Spy
    private JobMatchingService jobMatchingService;

    @InjectMocks
    private GetJobMatchingUseCase getJobMatchingUseCase;

    @Test
    void shouldReturnMatchingWhenUserAndJobExist() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID javaSkillId = UUID.randomUUID();
        UUID springSkillId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(User.create(
                "Nicolas",
                "nicolas@email.com",
                "123456",
                "Backend developer",
                "Remote",
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
        when(userSkillRepository.findByUserId(userId)).thenReturn(List.of(
                UserSkill.create(userId, javaSkillId, 4, 5),
                UserSkill.create(userId, springSkillId, 2, 3)
        ));
        when(jobRequirementRepository.findByJobId(jobId)).thenReturn(List.of(
                JobRequirement.create(jobId, javaSkillId, true, 4, 3),
                JobRequirement.create(jobId, springSkillId, false, 4, 2)
        ));

        JobMatchingResponse response = getJobMatchingUseCase.execute(userId, jobId);

        assertThat(response.score()).isEqualTo(90);
        assertThat(response.totalRequirements()).isEqualTo(2);
        assertThat(response.metRequirements()).isEqualTo(1);
        assertThat(response.unmetRequirements()).isEqualTo(1);
        assertThat(response.mustHaveUnmetRequirements()).isZero();
        verify(userSkillRepository).findByUserId(userId);
        verify(jobRequirementRepository).findByJobId(jobId);
    }

    @Test
    void shouldRejectMatchingWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getJobMatchingUseCase.execute(userId, jobId))
                .isInstanceOf(DomainException.class)
                .hasMessage("Usuário não encontrado pelo id " + userId);

        verify(jobRepository, never()).findById(jobId);
        verify(userSkillRepository, never()).findByUserId(userId);
        verify(jobRequirementRepository, never()).findByJobId(jobId);
    }

    @Test
    void shouldRejectMatchingWhenJobDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(User.create(
                "Nicolas",
                "nicolas@email.com",
                "123456",
                "Backend developer",
                "Remote",
                "Bio"
        )));
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getJobMatchingUseCase.execute(userId, jobId))
                .isInstanceOf(DomainException.class)
                .hasMessage("Vaga não encontrada pelo id " + jobId);

        verify(userSkillRepository, never()).findByUserId(userId);
        verify(jobRequirementRepository, never()).findByJobId(jobId);
    }

    @Test
    void shouldReturnZeroScoreWhenJobHasNoRequirements() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(User.create(
                "Nicolas",
                "nicolas@email.com",
                "123456",
                "Backend developer",
                "Remote",
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
        when(userSkillRepository.findByUserId(userId)).thenReturn(List.of(
                UserSkill.create(userId, UUID.randomUUID(), 4, 5)
        ));
        when(jobRequirementRepository.findByJobId(jobId)).thenReturn(List.of());

        JobMatchingResponse response = getJobMatchingUseCase.execute(userId, jobId);

        assertThat(response.score()).isZero();
        assertThat(response.totalRequirements()).isZero();
        assertThat(response.requirements()).isEmpty();
    }
}
