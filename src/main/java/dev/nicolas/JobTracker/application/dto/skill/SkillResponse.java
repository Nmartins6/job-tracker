package dev.nicolas.JobTracker.application.dto.skill;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record SkillResponse(
    UUID id,
    String name,
    String category
){}
