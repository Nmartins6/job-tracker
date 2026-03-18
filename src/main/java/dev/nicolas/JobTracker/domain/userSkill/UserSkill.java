package dev.nicolas.JobTracker.domain.userSkill;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;

import java.util.UUID;

public class UserSkill {

    private UUID id;
    private UUID userId;
    private UUID skillId;
    private Integer yearsExperience;
    private Integer level;

    private UserSkill() {

    }

    public static UserSkill create(UUID userId, UUID skillId, Integer yearsExperience, Integer level) {
        if (userId == null) {
            throw new DomainException("Usuário da habilidade é obrigatório");
        }
        if (skillId == null) {
            throw new DomainException("Habilidade é obrigatória");
        }
        if (yearsExperience == null || yearsExperience < 0) {
            throw new DomainException("Anos de experiência não pode ser negativo");
        }
        if (level == null || level < 1 || level > 5) {
            throw new DomainException("Nível da habilidade deve estar entre 1 e 5");
        }

        UserSkill userSkill = new UserSkill();
        userSkill.id = UUID.randomUUID();
        userSkill.userId = userId;
        userSkill.skillId = skillId;
        userSkill.yearsExperience = yearsExperience;
        userSkill.level = level;

        return userSkill;
    }

    public static UserSkill reconstitute(UUID id, UUID userId, UUID skillId, Integer yearsExperience, Integer level) {
        UserSkill userSkill = new UserSkill();
        userSkill.id = id;
        userSkill.userId = userId;
        userSkill.skillId = skillId;
        userSkill.yearsExperience = yearsExperience;
        userSkill.level = level;

        return userSkill;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getSkillId() {
        return skillId;
    }

    public Integer getYearsExperience() {
        return yearsExperience;
    }

    public Integer getLevel() {
        return level;
    }
}
