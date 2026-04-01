package dev.nicolas.JobTracker.domain.application;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;

import java.time.LocalDateTime;
import java.util.UUID;

public class Application {

    private UUID id;
    private UUID userId;
    private UUID jobId;
    private ApplicationStatus status;
    private String nextAction;
    private LocalDateTime nextActionDueAt;

    private Application() {

    }

    public static Application create(UUID userId, UUID jobId, String nextAction, LocalDateTime nextActionDueAt) {
        if (userId == null) {
            throw new DomainException("Usuário da candidatura é obrigatório");
        }
        if (jobId == null) {
            throw new DomainException("Vaga da candidatura é obrigatória");
        }
        String normalizedNextAction = normalizeNextAction(nextAction);
        validateNextAction(normalizedNextAction, nextActionDueAt);

        Application application = new Application();
        application.id = UUID.randomUUID();
        application.userId = userId;
        application.jobId = jobId;
        application.status = ApplicationStatus.ACTIVE;
        application.nextAction = normalizedNextAction;
        application.nextActionDueAt = nextActionDueAt;

        return application;
    }

    public static Application reconstitute(UUID id,
                                           UUID userId,
                                           UUID jobId,
                                           ApplicationStatus status,
                                           String nextAction,
                                           LocalDateTime nextActionDueAt) {
        Application application = new Application();
        application.id = id;
        application.userId = userId;
        application.jobId = jobId;
        application.status = status;
        application.nextAction = nextAction;
        application.nextActionDueAt = nextActionDueAt;

        return application;
    }

    public void updateTracking(UUID userId,
                               UUID jobId,
                               ApplicationStatus status,
                               String nextAction,
                               LocalDateTime nextActionDueAt) {
        if (userId == null) {
            throw new DomainException("Usuário da candidatura é obrigatório");
        }
        if (jobId == null) {
            throw new DomainException("Vaga da candidatura é obrigatória");
        }
        if (status == null) {
            throw new DomainException("Status da candidatura é obrigatório");
        }
        String normalizedNextAction = normalizeNextAction(nextAction);
        validateNextAction(normalizedNextAction, nextActionDueAt);

        this.userId = userId;
        this.jobId = jobId;
        this.status = status;
        this.nextAction = normalizedNextAction;
        this.nextActionDueAt = nextActionDueAt;
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

    public String getNextAction() {
        return nextAction;
    }

    public LocalDateTime getNextActionDueAt() {
        return nextActionDueAt;
    }

    private static String normalizeNextAction(String nextAction) {
        if (nextAction == null || nextAction.isBlank()) {
            return null;
        }

        return nextAction.trim();
    }

    private static void validateNextAction(String nextAction, LocalDateTime nextActionDueAt) {
        if (nextActionDueAt != null && nextAction == null) {
            throw new DomainException("Próxima ação é obrigatória quando a data é informada");
        }
    }
}
