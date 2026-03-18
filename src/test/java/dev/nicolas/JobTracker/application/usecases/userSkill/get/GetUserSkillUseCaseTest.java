package dev.nicolas.JobTracker.application.usecases.userSkill.get;

import dev.nicolas.JobTracker.application.dto.userSkill.UserSkillResponse;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.userSkill.UserSkill;
import dev.nicolas.JobTracker.domain.userSkill.UserSkillRepository;
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
class GetUserSkillUseCaseTest {

    @Mock
    private UserSkillRepository userSkillRepository;

    @InjectMocks
    private GetUserSkillUseCase getUserSkillUseCase;

    @Test
    void shouldReturnUserSkillById() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        UserSkill userSkill = UserSkill.reconstitute(id, userId, skillId, 5, 4);

        when(userSkillRepository.findById(id)).thenReturn(Optional.of(userSkill));

        UserSkillResponse response = getUserSkillUseCase.findById(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.skillId()).isEqualTo(skillId);
        assertThat(response.yearsExperience()).isEqualTo(5);
        assertThat(response.level()).isEqualTo(4);
    }

    @Test
    void shouldThrowWhenUserSkillIsNotFoundById() {
        UUID id = UUID.randomUUID();

        when(userSkillRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getUserSkillUseCase.findById(id))
                .isInstanceOf(DomainException.class)
                .hasMessage("Habilidade do usuário não encontrada pelo id " + id);
    }

    @Test
    void shouldReturnUserSkillsByUserId() {
        UUID userId = UUID.randomUUID();
        UserSkill first = UserSkill.reconstitute(UUID.randomUUID(), userId, UUID.randomUUID(), 3, 4);
        UserSkill second = UserSkill.reconstitute(UUID.randomUUID(), userId, UUID.randomUUID(), 1, 2);

        when(userSkillRepository.findByUserId(userId)).thenReturn(List.of(first, second));

        List<UserSkillResponse> responses = getUserSkillUseCase.findByUserId(userId);

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(UserSkillResponse::level)
                .containsExactly(4, 2);
    }
}
