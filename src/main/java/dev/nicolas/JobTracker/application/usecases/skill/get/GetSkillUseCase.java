package dev.nicolas.JobTracker.application.usecases.skill.get;

import dev.nicolas.JobTracker.application.dto.skill.SkillResponse;
import dev.nicolas.JobTracker.domain.skill.Skill;
import dev.nicolas.JobTracker.domain.skill.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetSkillUseCase {

    private final SkillRepository skillRepository;

    public GetSkillUseCase(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public List<SkillResponse> findAll() {
        return skillRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private SkillResponse toResponse(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getCategory()
        );
    }

}
