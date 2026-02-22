package dev.nicolas.JobTracker.application.dto.user;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String headline,
        String location,
        String bio){}
