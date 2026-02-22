CREATE TABLE skills
(
    id       UUID         NOT NULL,
    name     VARCHAR(100) NOT NULL,
    category VARCHAR(100),

    CONSTRAINT pk_skills PRIMARY KEY (id),
    CONSTRAINT uk_skills_name UNIQUE (name)
);