CREATE TABLE jobs (
    id          UUID PRIMARY KEY,
    company     VARCHAR(255) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    source_url  VARCHAR(255),
    seniority   VARCHAR(255),
    location    VARCHAR(255),
    description TEXT
);
