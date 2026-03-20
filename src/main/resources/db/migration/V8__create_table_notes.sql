CREATE TABLE notes
(
    id             UUID      NOT NULL,
    application_id UUID      NOT NULL,
    stage_id       UUID,
    content        TEXT      NOT NULL,
    created_at     TIMESTAMP NOT NULL,

    CONSTRAINT pk_notes PRIMARY KEY (id),
    CONSTRAINT fk_notes_application FOREIGN KEY (application_id) REFERENCES applications (id),
    CONSTRAINT fk_notes_stage FOREIGN KEY (stage_id) REFERENCES stages (id)
);
