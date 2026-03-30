package dev.nicolas.JobTracker.application.usecases.userSkill.update;

import dev.nicolas.JobTracker.application.dto.userSkill.UpdateUserSkillRequest;
import dev.nicolas.JobTracker.application.dto.userSkill.UserSkillResponse;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.skill.Skill;
import dev.nicolas.JobTracker.domain.skill.SkillRepository;
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
class UpdateUserSkillUseCaseTest {

    @Mock
    private UserSkillRepository userSkillRepository;

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private UpdateUserSkillUseCase updateUserSkillUseCase;

    @Test
    void shouldUpdateUserSkill() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID currentSkillId = UUID.randomUUID();
        UUID newSkillId = UUID.randomUUID();
        UserSkill userSkill = UserSkill.reconstitute(id, userId, currentSkillId, 2, 3);

        when(userSkillRepository.findById(id)).thenReturn(Optional.of(userSkill));
        when(skillRepository.findById(newSkillId)).thenReturn(Optional.of(Skill.create("Java", "Backend")));
        when(userSkillRepository.findByUserIdAndSkillId(userId, newSkillId)).thenReturn(Optional.empty());
        when(userSkillRepository.save(any(UserSkill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserSkillResponse response = updateUserSkillUseCase.execute(
                id,
                new UpdateUserSkillRequest(newSkillId, 5, 4)
        );

        ArgumentCaptor<UserSkill> userSkillCaptor = ArgumentCaptor.forClass(UserSkill.class);
        verify(userSkillRepository).save(userSkillCaptor.capture());

        assertThat(userSkillCaptor.getValue().getSkillId()).isEqualTo(newSkillId);
        assertThat(userSkillCaptor.getValue().getYearsExperience()).isEqualTo(5);
        assertThat(userSkillCaptor.getValue().getLevel()).isEqualTo(4);
        assertThat(response.skillId()).isEqualTo(newSkillId);
        assertThat(response.yearsExperience()).isEqualTo(5);
        assertThat(response.level()).isEqualTo(4);
    }

    @Test
    void shouldRejectUpdateWhenUserSkillDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(userSkillRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateUserSkillUseCase.execute(
                id,
                new UpdateUserSkillRequest(UUID.randomUUID(), 2, 3)
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Habilidade do usuário não encontrada pelo id " + id);

        verify(userSkillRepository, never()).save(any(UserSkill.class));
    }

    @Test
    void shouldRejectUpdateWhenSkillAlreadyExistsForUser() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID currentSkillId = UUID.randomUUID();
        UUID newSkillId = UUID.randomUUID();
        UserSkill existing = UserSkill.reconstitute(id, userId, currentSkillId, 2, 3);

        when(userSkillRepository.findById(id)).thenReturn(Optional.of(existing));
        when(skillRepository.findById(newSkillId)).thenReturn(Optional.of(Skill.create("Spring", "Backend")));
        when(userSkillRepository.findByUserIdAndSkillId(userId, newSkillId)).thenReturn(
                Optional.of(UserSkill.reconstitute(UUID.randomUUID(), userId, newSkillId, 4, 5))
        );

        assertThatThrownBy(() -> updateUserSkillUseCase.execute(
                id,
                new UpdateUserSkillRequest(newSkillId, 4, 5)
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("Habilidade já cadastrada para o usuário");

        verify(userSkillRepository, never()).save(any(UserSkill.class));
    }
}
