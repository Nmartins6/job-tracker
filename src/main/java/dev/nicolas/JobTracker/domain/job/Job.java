package dev.nicolas.JobTracker.domain.job;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;

import java.util.UUID;

public class Job {

    private UUID id;
    private String company;
    private String title;
    private String sourceUrl;
    private String seniority;
    private String location;
    private String description;

    private Job() {

    }

    public static Job create(String company, String title, String sourceUrl,
                             String seniority, String location, String description) {
        if (company == null || company.isBlank()) {
            throw new DomainException("Nome da empresa é obrigatório!");
        }
        if (title == null || title.isBlank()) {
            throw new DomainException("Titulo é obrigatório");
        }

        Job job = new Job();
        job.id = UUID.randomUUID();
        job.company = company.trim();
        job.title = title.trim();
        job.sourceUrl = sourceUrl != null ? sourceUrl.trim() : null;
        job.seniority = seniority != null ? seniority.trim() : null;
        job.location = location != null ? location.trim() : null;
        job.description = description;

        return job;
    }

    public static Job reconstitute(UUID id, String company, String title, String sourceUrl,
                                   String seniority, String location, String description) {
        Job job = new Job();
        job.id = id;
        job.company = company;
        job.title = title;
        job.sourceUrl = sourceUrl;
        job.seniority = seniority;
        job.location = location;
        job.description = description;
        return job;
    }

    public UUID getId() {
        return id;
    }

    public String getCompany() {
        return company;
    }

    public String getTitle() {
        return title;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getLocation() {
        return location;
    }

    public String getSeniority() {
        return seniority;
    }

    public String getDescription() {
        return description;
    }

}
