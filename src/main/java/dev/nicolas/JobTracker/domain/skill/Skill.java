package dev.nicolas.JobTracker.domain.skill;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;

import java.util.UUID;

public class Skill {

    private UUID id;
    private String name;
    private String category;

    private Skill(){

    }

    public static Skill create(String name, String category) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Nome da habilidade precisa ser informada");
        }

        Skill skill = new Skill();
        skill.id = UUID.randomUUID();
        skill.name = name.trim();
        skill.category = category;

        return skill;
    }

    public static Skill reconstitute(UUID id, String name, String category) {
        Skill skill = new Skill();
        skill.id = id;
        skill.name = name;
        skill.category = category;

        return skill;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}
