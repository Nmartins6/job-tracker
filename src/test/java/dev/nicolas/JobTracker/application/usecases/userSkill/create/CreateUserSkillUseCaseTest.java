package dev.nicolas.JobTracker.application.usecases.userSkill.create;

import dev.nicolas.JobTracker.application.dto.userSkill.CreateUserSkillRequest;
import dev.nicolas.JobTracker.application.dto.userSkill.UserSkillResponse;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.skill.Skill;
import dev.nicolas.JobTracker.domain.skill.SkillRepository;
import dev.nicolas.JobTracker.domain.user.User;
import dev.nicolas.JobTracker.domain.user.UserRepository;
import dev.nicolas.JobTracker.domain.userSkill.UserSkill;
import dev.nicolas.JobTracker.domain.userSkill.UserSkillRepository;
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
class CreateUserSkillUseCaseTest {

    @Mock
    private UserSkillRepository userSkillRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private CreateUserSkillUseCase createUserSkillUseCase;

    @Test
    void shouldCreateUserSkillWhenUserAndSkillExist() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        CreateUserSkillRequest request = new CreateUserSkillRequest(userId, skillId, 3, 4);

        when(userRepository.findById(userId)).thenReturn(Optional.of(User.create(
                "Nicolas",
                "nicolas@example.com",
                "123456",
                "Backend Developer",
                "Brazil",
                "Bio"
        )));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(Skill.create("Java", "Backend")));
        when(userSkillRepository.findByUserIdAndSkillId(userId, skillId)).thenReturn(Optional.empty());
        when(userSkillRepository.save(any(UserSkill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserSkillResponse response = createUserSkillUseCase.execute(request);

        ArgumentCaptor<UserSkill> userSkillCaptor = ArgumentCaptor.forClass(UserSkill.class);
        verify(userSkillRepository).save(userSkillCaptor.capture());

        UserSkill savedUserSkill = userSkillCaptor.getValue();
        assertThat(savedUserSkill.getUserId()).isEqualTo(userId);
        assertThat(savedUserSkill.getSkillId()).isEqualTo(skillId);
        assertThat(savedUserSkill.getYearsExperience()).isEqualTo(3);
        assertThat(savedUserSkill.getLevel()).isEqualTo(4);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.skillId()).isEqualTo(skillId);
        assertThat(response.yearsExperience()).isEqualTo(3);
        assertThat(response.level()).isEqualTo(4);
    }

    @Test
    void shouldRejectUserSkillWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createUserSkillUseCase.execute(
                new CreateUserSkillRequest(userId, skillId, 2, 3)
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Usuário não encontrado pelo id " + userId);

        verify(skillRepository, never()).findById(skillId);
        verify(userSkillRepository, never()).save(any(UserSkill.class));
    }

    @Test
    void shouldRejectUserSkillWhenSkillDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(User.create(
                "Nicolas",
                "nicolas@example.com",
                "123456",
                "Backend Developer",
                "Brazil",
                "Bio"
        )));
        when(skillRepository.findById(skillId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createUserSkillUseCase.execute(
                new CreateUserSkillRequest(userId, skillId, 2, 3)
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Habilidade não encontrada pelo id " + skillId);

        verify(userSkillRepository, never()).save(any(UserSkill.class));
    }

    @Test
    void shouldRejectUserSkillWhenSkillAlreadyExistsForUser() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(User.create(
                "Nicolas",
                "nicolas@example.com",
                "123456",
                "Backend Developer",
                "Brazil",
                "Bio"
        )));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(Skill.create("Java", "Backend")));
        when(userSkillRepository.findByUserIdAndSkillId(userId, skillId)).thenReturn(
                Optional.of(UserSkill.reconstitute(UUID.randomUUID(), userId, skillId, 2, 4))
        );

        assertThatThrownBy(() -> createUserSkillUseCase.execute(
                new CreateUserSkillRequest(userId, skillId, 2, 4)
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Habilidade já cadastrada para o usuário");

        verify(userSkillRepository, never()).save(any(UserSkill.class));
    }
}
