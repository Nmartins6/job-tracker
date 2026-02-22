package dev.nicolas.JobTracker.application.usecases.skill.create;

import dev.nicolas.JobTracker.application.dto.skill.CreateSkillRequest;
import dev.nicolas.JobTracker.application.dto.skill.SkillResponse;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.skill.Skill;
import dev.nicolas.JobTracker.domain.skill.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateSkillUseCase {

    private final SkillRepository skillRepository;

    public CreateSkillUseCase (SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Transactional
    public SkillResponse execute(CreateSkillRequest request) {
        if (skillRepository.existsByName(request.name())){
            throw new DomainException("Habilidade já cadastrada");
        }

        Skill skill = Skill.create(request.name(), request.category());
        Skill saved = skillRepository.save(skill);

        return new SkillResponse(
                saved.getId(),
                saved.getName(),
                saved.getCategory()
        );
    }

}
