CREATE TABLE applications
(
    id      UUID        NOT NULL,
    user_id UUID        NOT NULL,
    job_id  UUID        NOT NULL,
    status  VARCHAR(50) NOT NULL,

    CONSTRAINT pk_applications PRIMARY KEY (id),
    CONSTRAINT fk_applications_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_applications_job FOREIGN KEY (job_id) REFERENCES jobs (id)
);
