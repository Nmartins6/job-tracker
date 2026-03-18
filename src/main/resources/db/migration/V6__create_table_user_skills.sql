CREATE TABLE user_skills
(
    id               UUID    NOT NULL,
    user_id          UUID    NOT NULL,
    skill_id         UUID    NOT NULL,
    years_experience INTEGER NOT NULL,
    level            INTEGER NOT NULL,

    CONSTRAINT pk_user_skills PRIMARY KEY (id),
    CONSTRAINT fk_user_skills_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id),
    CONSTRAINT uq_user_skills_user_skill UNIQUE (user_id, skill_id),
    CONSTRAINT ck_user_skills_years_experience_non_negative CHECK (years_experience >= 0),
    CONSTRAINT ck_user_skills_level_range CHECK (level BETWEEN 1 AND 5)
);
