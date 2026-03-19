CREATE TABLE job_requirements
(
    id            UUID    NOT NULL,
    job_id        UUID    NOT NULL,
    skill_id      UUID    NOT NULL,
    must_have     BOOLEAN NOT NULL,
    desired_level INTEGER NOT NULL,
    weight        INTEGER NOT NULL,

    CONSTRAINT pk_job_requirements PRIMARY KEY (id),
    CONSTRAINT fk_job_requirements_job FOREIGN KEY (job_id) REFERENCES jobs (id),
    CONSTRAINT fk_job_requirements_skill FOREIGN KEY (skill_id) REFERENCES skills (id),
    CONSTRAINT uq_job_requirements_job_skill UNIQUE (job_id, skill_id),
    CONSTRAINT ck_job_requirements_desired_level_range CHECK (desired_level BETWEEN 1 AND 5),
    CONSTRAINT ck_job_requirements_weight_positive CHECK (weight >= 1)
);
