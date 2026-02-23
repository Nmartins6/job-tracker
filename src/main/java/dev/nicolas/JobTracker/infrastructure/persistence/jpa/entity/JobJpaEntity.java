package dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String title;

    @Column(name = "source_url")
    private String sourceUrl;

    private String seniority;

    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;
}
