package dev.nicolas.JobTracker.application.usecases.userSkill.update;

import dev.nicolas.JobTracker.application.dto.userSkill.UpdateUserSkillRequest;
import dev.nicolas.JobTracker.application.dto.userSkill.UserSkillResponse;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.skill.SkillRepository;
import dev.nicolas.JobTracker.domain.userSkill.UserSkill;
import dev.nicolas.JobTracker.domain.userSkill.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateUserSkillUseCase {

    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;

    public UpdateUserSkillUseCase(UserSkillRepository userSkillRepository,
                                  SkillRepository skillRepository) {
        this.userSkillRepository = userSkillRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional
    public UserSkillResponse execute(UUID id, UpdateUserSkillRequest request) {
        UserSkill existing = userSkillRepository.findById(id)
                .orElseThrow(() -> new DomainException("Habilidade do usuário não encontrada pelo id " + id));

        if (skillRepository.findById(request.skillId()).isEmpty()) {
            throw new DomainException("Habilidade não encontrada pelo id " + request.skillId());
        }

        userSkillRepository.findByUserIdAndSkillId(existing.getUserId(), request.skillId())
                .filter(userSkill -> !userSkill.getId().equals(id))
                .ifPresent(userSkill -> {
                    throw new DomainException("Habilidade já cadastrada para o usuário");
                });

        existing.updateProfile(
                request.skillId(),
                request.yearsExperience(),
                request.level()
        );

        UserSkill saved = userSkillRepository.save(existing);

        return new UserSkillResponse(
                saved.getId(),
                saved.getUserId(),
                saved.getSkillId(),
                saved.getYearsExperience(),
                saved.getLevel()
        );
    }
}
