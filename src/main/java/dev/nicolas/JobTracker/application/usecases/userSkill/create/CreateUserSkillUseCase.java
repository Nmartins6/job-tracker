package dev.nicolas.JobTracker.application.usecases.userSkill.create;

import dev.nicolas.JobTracker.application.dto.userSkill.CreateUserSkillRequest;
import dev.nicolas.JobTracker.application.dto.userSkill.UserSkillResponse;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.skill.SkillRepository;
import dev.nicolas.JobTracker.domain.user.UserRepository;
import dev.nicolas.JobTracker.domain.userSkill.UserSkill;
import dev.nicolas.JobTracker.domain.userSkill.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateUserSkillUseCase {

    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    public CreateUserSkillUseCase(UserSkillRepository userSkillRepository,
                                  UserRepository userRepository,
                                  SkillRepository skillRepository) {
        this.userSkillRepository = userSkillRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional
    public UserSkillResponse execute(CreateUserSkillRequest request) {
        if (userRepository.findById(request.userId()).isEmpty()) {
            throw new DomainException("Usuário não encontrado pelo id " + request.userId());
        }

        if (skillRepository.findById(request.skillId()).isEmpty()) {
            throw new DomainException("Habilidade não encontrada pelo id " + request.skillId());
        }

        if (userSkillRepository.findByUserIdAndSkillId(request.userId(), request.skillId()).isPresent()) {
            throw new DomainException("Habilidade já cadastrada para o usuário");
        }

        UserSkill userSkill = UserSkill.create(
                request.userId(),
                request.skillId(),
                request.yearsExperience(),
                request.level()
        );
        UserSkill saved = userSkillRepository.save(userSkill);

        return toResponse(saved);
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
