package dev.nicolas.JobTracker.application.usecases.user.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String headline,
        String location,
        String bio){}
