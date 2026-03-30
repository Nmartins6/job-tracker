package dev.nicolas.JobTracker.domain.application;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;

import java.util.UUID;

public class Application {

    private UUID id;
    private UUID userId;
    private UUID jobId;
    private ApplicationStatus status;

    private Application() {

    }

    public static Application create(UUID userId, UUID jobId) {
        if (userId == null) {
            throw new DomainException("Usuário da candidatura é obrigatório");
        }
        if (jobId == null) {
            throw new DomainException("Vaga da candidatura é obrigatória");
        }

        Application application = new Application();
        application.id = UUID.randomUUID();
        application.userId = userId;
        application.jobId = jobId;
        application.status = ApplicationStatus.ACTIVE;

        return application;
    }

    public static Application reconstitute(UUID id, UUID userId, UUID jobId, ApplicationStatus status) {
        Application application = new Application();
        application.id = id;
        application.userId = userId;
        application.jobId = jobId;
        application.status = status;

        return application;
    }

    public void updateTracking(UUID userId, UUID jobId, ApplicationStatus status) {
        if (userId == null) {
            throw new DomainException("Usuário da candidatura é obrigatório");
        }
        if (jobId == null) {
            throw new DomainException("Vaga da candidatura é obrigatória");
        }
        if (status == null) {
            throw new DomainException("Status da candidatura é obrigatório");
        }

        this.userId = userId;
        this.jobId = jobId;
        this.status = status;
    }

    public void updateStatus(ApplicationStatus status) {
        if (status == null) {
            throw new DomainException("Status da candidatura é obrigatório");
        }

        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }
}
