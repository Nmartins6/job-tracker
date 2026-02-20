CREATE TABLE users
(
    id            UUID         NOT NULL,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    head_line     VARCHAR(255),
    location      VARCHAR(255),
    bio           TEXT,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);