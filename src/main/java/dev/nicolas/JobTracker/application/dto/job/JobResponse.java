package dev.nicolas.JobTracker.application.dto.job;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record JobResponse(
    UUID id,
    String company,
    String title,
    String sourceUrl,
    String seniority,
    String location,
    String description
){}
