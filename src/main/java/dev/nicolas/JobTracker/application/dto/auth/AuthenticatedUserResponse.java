package dev.nicolas.JobTracker.application.dto.auth;

import java.util.UUID;

public record AuthenticatedUserResponse(
        UUID id,
        String name,
        String email
) {
}
