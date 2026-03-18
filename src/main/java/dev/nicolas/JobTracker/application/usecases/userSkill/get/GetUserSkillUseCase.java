package dev.nicolas.JobTracker.application.usecases.userSkill.get;

import dev.nicolas.JobTracker.application.dto.userSkill.UserSkillResponse;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.userSkill.UserSkill;
import dev.nicolas.JobTracker.domain.userSkill.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetUserSkillUseCase {

    private final UserSkillRepository userSkillRepository;

    public GetUserSkillUseCase(UserSkillRepository userSkillRepository) {
        this.userSkillRepository = userSkillRepository;
    }

    public UserSkillResponse findById(UUID id) {
        UserSkill userSkill = userSkillRepository.findById(id)
                .orElseThrow(() -> new DomainException("Habilidade do usuário não encontrada pelo id " + id));

        return toResponse(userSkill);
    }

    public List<UserSkillResponse> findByUserId(UUID userId) {
        return userSkillRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private UserSkillResponse toResponse(UserSkill userSkill) {
        return new UserSkillResponse(
                userSkill.getId(),
                userSkill.getUserId(),
                userSkill.getSkillId(),
                userSkill.getYearsExperience(),
                userSkill.getLevel()
        );
    }
}
