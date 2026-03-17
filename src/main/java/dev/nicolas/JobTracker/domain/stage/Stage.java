package dev.nicolas.JobTracker.domain.stage;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;

import java.time.LocalDateTime;
import java.util.UUID;

public class Stage {

    private UUID id;
    private UUID applicationId;
    private String name;
    private Integer orderIndex;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime deadlineAt;

    private Stage() {

    }

    public static Stage create(UUID applicationId, String name, Integer orderIndex, LocalDateTime deadlineAt) {
        if (applicationId == null) {
            throw new DomainException("Candidatura da etapa é obrigatória");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("Nome da etapa é obrigatório");
        }
        if (orderIndex == null || orderIndex < 1) {
            throw new DomainException("Ordem da etapa deve ser maior que zero");
        }

        Stage stage = new Stage();
        stage.id = UUID.randomUUID();
        stage.applicationId = applicationId;
        stage.name = name.trim();
        stage.orderIndex = orderIndex;
        stage.deadlineAt = deadlineAt;

        return stage;
    }

    public static Stage reconstitute(UUID id,
                                     UUID applicationId,
                                     String name,
                                     Integer orderIndex,
                                     LocalDateTime startedAt,
                                     LocalDateTime completedAt,
                                     LocalDateTime deadlineAt) {
        Stage stage = new Stage();
        stage.id = id;
        stage.applicationId = applicationId;
        stage.name = name;
        stage.orderIndex = orderIndex;
        stage.startedAt = startedAt;
        stage.completedAt = completedAt;
        stage.deadlineAt = deadlineAt;

        return stage;
    }

    public void start(LocalDateTime startedAt) {
        if (startedAt == null) {
            throw new DomainException("Data de início da etapa é obrigatória");
        }
        if (this.startedAt != null) {
            throw new DomainException("Etapa já iniciada");
        }

        this.startedAt = startedAt;
    }

    public void complete(LocalDateTime completedAt) {
        if (completedAt == null) {
            throw new DomainException("Data de conclusão da etapa é obrigatória");
        }
        if (startedAt == null) {
            throw new DomainException("Etapa precisa ser iniciada antes de ser concluída");
        }
        if (this.completedAt != null) {
            throw new DomainException("Etapa já concluída");
        }
        if (completedAt.isBefore(startedAt)) {
            throw new DomainException("Conclusão da etapa não pode ser anterior ao início");
        }

        this.completedAt = completedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public String getName() {
        return name;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getDeadlineAt() {
        return deadlineAt;
    }
}
