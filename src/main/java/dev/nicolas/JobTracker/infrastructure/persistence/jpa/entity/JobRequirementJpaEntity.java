package dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "job_requirements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequirementJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "skill_id", nullable = false)
    private UUID skillId;

    @Column(name = "must_have", nullable = false)
    private boolean mustHave;

    @Column(name = "desired_level", nullable = false)
    private Integer desiredLevel;

    @Column(nullable = false)
    private Integer weight;
}
