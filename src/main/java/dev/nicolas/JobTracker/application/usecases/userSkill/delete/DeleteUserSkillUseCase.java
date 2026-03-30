package dev.nicolas.JobTracker.application.usecases.userSkill.delete;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.userSkill.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteUserSkillUseCase {

    private final UserSkillRepository userSkillRepository;

    public DeleteUserSkillUseCase(UserSkillRepository userSkillRepository) {
        this.userSkillRepository = userSkillRepository;
    }

    @Transactional
    public void execute(UUID id) {
        if (userSkillRepository.findById(id).isEmpty()) {
            throw new DomainException("Habilidade do usuário não encontrada pelo id " + id);
        }

        userSkillRepository.deleteById(id);
    }
}
