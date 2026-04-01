ALTER TABLE applications
    ADD COLUMN next_action VARCHAR(255);

ALTER TABLE applications
    ADD COLUMN next_action_due_at TIMESTAMP;
