CREATE TABLE stages
(
    id             UUID         NOT NULL,
    application_id UUID         NOT NULL,
    name           VARCHAR(255) NOT NULL,
    order_index    INTEGER      NOT NULL,
    started_at     TIMESTAMP,
    completed_at   TIMESTAMP,
    deadline_at    TIMESTAMP,

    CONSTRAINT pk_stages PRIMARY KEY (id),
    CONSTRAINT fk_stages_application FOREIGN KEY (application_id) REFERENCES applications (id),
    CONSTRAINT uq_stages_application_order UNIQUE (application_id, order_index),
    CONSTRAINT ck_stages_order_index_positive CHECK (order_index > 0)
);
