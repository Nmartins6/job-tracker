package dev.nicolas.JobTracker.domain.jobRequirement;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;

import java.util.UUID;

public class JobRequirement {

    private UUID id;
    private UUID jobId;
    private UUID skillId;
    private boolean mustHave;
    private Integer desiredLevel;
    private Integer weight;

    private JobRequirement() {

    }

    public static JobRequirement create(UUID jobId,
                                        UUID skillId,
                                        boolean mustHave,
                                        Integer desiredLevel,
                                        Integer weight) {
        if (jobId == null) {
            throw new DomainException("Vaga do requisito é obrigatória");
        }
        if (skillId == null) {
            throw new DomainException("Habilidade do requisito é obrigatória");
        }
        if (desiredLevel == null || desiredLevel < 1 || desiredLevel > 5) {
            throw new DomainException("Nível desejado deve estar entre 1 e 5");
        }
        if (weight == null || weight < 1) {
            throw new DomainException("Peso do requisito deve ser maior que zero");
        }

        JobRequirement jobRequirement = new JobRequirement();
        jobRequirement.id = UUID.randomUUID();
        jobRequirement.jobId = jobId;
        jobRequirement.skillId = skillId;
        jobRequirement.mustHave = mustHave;
        jobRequirement.desiredLevel = desiredLevel;
        jobRequirement.weight = weight;

        return jobRequirement;
    }

    public static JobRequirement reconstitute(UUID id,
                                              UUID jobId,
                                              UUID skillId,
                                              boolean mustHave,
                                              Integer desiredLevel,
                                              Integer weight) {
        JobRequirement jobRequirement = new JobRequirement();
        jobRequirement.id = id;
        jobRequirement.jobId = jobId;
        jobRequirement.skillId = skillId;
        jobRequirement.mustHave = mustHave;
        jobRequirement.desiredLevel = desiredLevel;
        jobRequirement.weight = weight;

        return jobRequirement;
    }

    public UUID getId() {
        return id;
    }

    public UUID getJobId() {
        return jobId;
    }

    public UUID getSkillId() {
        return skillId;
    }

    public boolean isMustHave() {
        return mustHave;
    }

    public Integer getDesiredLevel() {
        return desiredLevel;
    }

    public Integer getWeight() {
        return weight;
    }
}
